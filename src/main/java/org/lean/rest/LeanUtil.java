package org.lean.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hop.core.database.Database;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.encryption.HopTwoWayPasswordEncoder;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.logging.LoggingObject;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.metadata.api.IHopMetadata;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.metadata.api.IHopMetadataSerializer;
import org.apache.hop.metadata.serializer.json.JsonMetadataProvider;
import org.lean.core.LeanAttachment;
import org.lean.core.LeanColorRGB;
import org.lean.core.LeanColumn;
import org.lean.core.LeanDatabaseConnection;
import org.lean.core.LeanEnvironment;
import org.lean.core.LeanFont;
import org.lean.core.LeanHorizontalAlignment;
import org.lean.core.LeanSortMethod;
import org.lean.core.LeanVerticalAlignment;
import org.lean.core.draw.DrawnItem;
import org.lean.core.exception.LeanException;
import org.lean.presentation.LeanPresentation;
import org.lean.presentation.component.LeanComponent;
import org.lean.presentation.component.types.table.LeanTableComponent;
import org.lean.presentation.connector.LeanConnector;
import org.lean.presentation.connector.types.metadata.LeanMetadataPresentationsConnector;
import org.lean.presentation.connector.types.sampledata.LeanSampleDataConnector;
import org.lean.presentation.connector.types.sort.LeanSortConnector;
import org.lean.presentation.connector.types.sql.LeanSqlConnector;
import org.lean.presentation.interaction.LeanInteraction;
import org.lean.presentation.interaction.LeanInteractionAction;
import org.lean.presentation.interaction.LeanInteractionLocation;
import org.lean.presentation.interaction.LeanInteractionMethod;
import org.lean.presentation.layout.LeanLayout;
import org.lean.presentation.layout.LeanLayoutResults;
import org.lean.presentation.layout.LeanRenderPage;
import org.lean.presentation.page.LeanPage;
import org.lean.presentation.theme.LeanTheme;
import org.lean.render.IRenderContext;
import org.lean.render.context.PresentationRenderContext;
import org.lean.rest.render.IRendering;

public class LeanUtil {
  public static final String CONNECTOR_STEEL_WHEELS_NAME = "SteelWheels";

  private static LeanUtil leanUtil;
  private final LoggingObject loggingObject;
  private final IVariables variables;
  private final IHopMetadataProvider metadataProvider;
  private final LogChannel log;
  private final String metadataPath;

  private IHopMetadataSerializer<LeanPresentation> presentationSerializer;
  private IHopMetadataSerializer<LeanDatabaseConnection> dbConnSerializer;
  private IHopMetadataSerializer<LeanTheme> themeSerializer;
  private IHopMetadataSerializer<IHopMetadata> metadataSerializer;

  private Map<String, IRendering> renderCache;

  public LeanUtil() {
    loggingObject = new LoggingObject("Lean Rest");
    log = new LogChannel("Lean Rest Server");
    renderCache = new HashMap<>();
    try {
      LeanEnvironment.init();
    } catch (Exception e) {
      log.logError("Could not initialize the Lean environment", e);
    }
    log.logBasic("Starting the Lean REST services application.");
    Properties props = new Properties();
    String propertyPath;
    try {
      if (StringUtils.isEmpty(System.getProperty("CONFIG_PATH"))) {
        propertyPath = "/config";
      } else {
        propertyPath = System.getProperty("CONFIG_PATH");
      }
      String configFileName = propertyPath + "/leanrest.properties";
      File configFile = new File(configFileName);
      if (configFile.exists()) {
        try (InputStream inputStream = new FileInputStream(configFile)) {
          props.load(inputStream);
        }
      } else {
        try (InputStream inputStream = getClass().getResourceAsStream("/leanrest.properties")) {
          props.load(inputStream);
        }
      }
    } catch (IOException e) {
      log.logError("Error initializing Lean Rest: ", e);
    }
    metadataPath = props.getProperty("metadata.path");

    variables = Variables.getADefaultVariableSpace();
    metadataProvider =
        new JsonMetadataProvider(new HopTwoWayPasswordEncoder(), metadataPath, variables);

    log.logBasic("Found " + metadataProvider.getMetadataClasses().size() + " metadata types.");

    try {
      presentationSerializer = metadataProvider.getSerializer(LeanPresentation.class);
      dbConnSerializer = metadataProvider.getSerializer(LeanDatabaseConnection.class);
      themeSerializer = metadataProvider.getSerializer(LeanTheme.class);

      createConnections();
      createListPresentation();
      importPresentations();
    } catch (Exception e) {
      log.logError("Error creating presentation serializer: ", e);
    }
  }

  private void createListPresentation() throws Exception {

    IHopMetadataSerializer<LeanConnector> serializer =
        metadataProvider.getSerializer(LeanConnector.class);

    // Add a few connectors to the metadata
    //
    String presentationListConnectorName = "presentation-list";
    LeanMetadataPresentationsConnector presentationsListConnector =
        new LeanMetadataPresentationsConnector();
    LeanConnector presentationsList =
        new LeanConnector(presentationListConnectorName, presentationsListConnector);
    serializer.save(presentationsList);

    // We get the data from a connector
    //
    String connectorName = "presentation-list-sorted";
    LeanSortConnector sortConnector =
        new LeanSortConnector(
            List.of(new LeanColumn("name")),
            List.of(new LeanSortMethod(LeanSortMethod.Type.STRING_ALPHA_CASE_INSENSITIVE, true)));
    sortConnector.setSourceConnectorName(presentationListConnectorName);
    LeanConnector presentationsListSorted = new LeanConnector(connectorName, sortConnector);
    serializer.save(presentationsListSorted);

    // Create a presentation to list the available presentations
    //
    LeanPresentation presentation = new LeanPresentation();
    presentation.setName("list-presentations");
    LeanTheme theme = LeanTheme.getDefault();
    presentation.getThemes().add(theme);
    presentation.setDefaultThemeName(theme.getName());

    // Add one page
    LeanPage page = LeanPage.getA4(1, false);
    presentation.getPages().add(page);

    // Italic font
    //
    LeanFont italicFont = new LeanFont(theme.getDefaultFont());
    italicFont.setItalic(true);

    // Add a table component listing the names
    //
    String componentName = "list-presentations";
    LeanTableComponent tableComponent =
        new LeanTableComponent(
            connectorName,
            List.of(
                new LeanColumn(
                    "name",
                    "Presentation name",
                    LeanHorizontalAlignment.LEFT,
                    LeanVerticalAlignment.MIDDLE),
                new LeanColumn(
                    "description",
                    "Description",
                    LeanHorizontalAlignment.LEFT,
                    LeanVerticalAlignment.MIDDLE)));
    tableComponent.setEvenHeights(true);
    tableComponent.setHeader(true);
    tableComponent.setHeader(true);
    tableComponent.setHeaderFont(theme.getHorizontalDimensionsFont());
    tableComponent.setDefaultFont(italicFont);
    tableComponent.setHorizontalMargin(2);
    tableComponent.setVerticalMargin(2);
    tableComponent.setBackground(true);
    tableComponent.setHeaderBackGroundColor(new LeanColorRGB(220, 220, 255));
    tableComponent.setBackGroundColor(LeanColorRGB.WHITE);
    LeanComponent component = new LeanComponent(componentName, tableComponent);
    component.setLayout(
        new LeanLayout(
            new LeanAttachment(0, 0),
            new LeanAttachment(100, 0),
            new LeanAttachment(0, 0),
            new LeanAttachment(100, 0)));
    page.getComponents().add(component);

    // Add an interaction:
    //  - Method: single left click
    //  - Location: in the table component on a cell
    //  - Action: open the presentation with the name in the cell value.
    //
    LeanInteraction openPresentationInteraction = new LeanInteraction();
    LeanInteractionLocation location =
        new LeanInteractionLocation(
            componentName,
            "LeanTableComponent",
            DrawnItem.DrawnItemType.ComponentItem.name(),
            DrawnItem.Category.Cell.name(),
            List.of("name"));
    LeanInteractionAction action =
        new LeanInteractionAction(LeanInteractionAction.ActionType.OPEN_PRESENTATION);

    LeanInteractionMethod method = new LeanInteractionMethod(true, false);
    openPresentationInteraction.setMethod(method);
    openPresentationInteraction.setLocation(location);
    openPresentationInteraction.getActions().add(action);
    presentation.getInteractions().add(openPresentationInteraction);

    // Save the presentation
    presentationSerializer.save(presentation);
  }

  private void importPresentations() throws Exception {
    for (String filename :
        List.of("import/combo-test.json", "import/grouped-composite-test.json")) {
      try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename)) {
        String json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        LeanPresentation presentation = LeanPresentation.fromJsonString(json);
        if (presentationSerializer.exists(presentation.getName())) {
          presentationSerializer.delete(presentation.getName());
        }
        presentationSerializer.save(presentation);
      }
    }
  }

  public static LeanUtil getInstance() {
    if (leanUtil == null) {
      leanUtil = new LeanUtil();
    }
    return leanUtil;
  }

  public LeanPresentation loadPresentation(String presentationName)
      throws LeanException, HopException {
    LeanPresentation presentation = presentationSerializer.load(presentationName);
    if (presentation == null) {
      throw new LeanException("Unable to find presentation '" + presentationName + "'");
    }
    return presentation;
  }

  private List<LeanRenderPage> renderPresentation(String presentationName)
      throws LeanException, HopException {
    LeanPresentation presentation = loadPresentation(presentationName);
    IRenderContext renderContext = new PresentationRenderContext(presentation);
    LeanLayoutResults layout =
        presentation.doLayout(
            loggingObject, renderContext, metadataProvider, Collections.emptyList());
    presentation.render(layout, metadataProvider);
    return layout.getRenderPages();
  }

  public String getPresentationSVG(String presentationName, int pageNumber)
      throws HopException, LeanException {
    log.logBasic("Loading presentation " + presentationName);

    List<LeanRenderPage> renderPages = renderPresentation(presentationName);
    return renderPages.get(pageNumber).getSvgXml();
  }

  public List<String> getComponentsAt(String presentationName, int pageNb, String posXY)
      throws HopException, LeanException {
    int posX = Integer.parseInt(posXY.split(",")[0]);
    int posY = Integer.parseInt(posXY.split(",")[1]);

    List<LeanRenderPage> renderPages = renderPresentation(presentationName);
    LeanRenderPage renderPage = renderPages.get(pageNb);

    return renderPage.lookupComponentName(posX, posY);
  }

  private void createConnections() throws HopException {
    LeanDatabaseConnection connection2 =
        new LeanDatabaseConnection(
            "logging", "POSTGRESQL", "localhost", "5432", "logging", "postgres", "postgres");
    String h2DatabaseName =
        System.getProperty("java.io.tmpdir") + File.separator + CONNECTOR_STEEL_WHEELS_NAME;

    LeanDatabaseConnection swConnection =
        new LeanDatabaseConnection(
            CONNECTOR_STEEL_WHEELS_NAME, "H2", null, null, h2DatabaseName, null, null);

    try {
      dbConnSerializer.save(connection2);
      dbConnSerializer.save(swConnection);
    } catch (HopException e) {
      log.logError("Error saving dummy connections: " + e.getMessage());
    }

    try {
      // Delete old database
      //
      File[] files =
          new File(System.getProperty("java.io.tmpdir"))
              .listFiles(
                  pathname ->
                      pathname.toString().endsWith(".db")
                          && pathname.toString().contains(CONNECTOR_STEEL_WHEELS_NAME));
      for (File file : files) {
        FileUtils.forceDelete(file);
      }

      // Read the script
      //
      List<String> lines =
          Files.readAllLines(
              Paths.get(getClass().getClassLoader().getResource("steelwheels.script").getPath()));

      DatabaseMeta databaseMeta = swConnection.createDatabaseMeta();
      databaseMeta.setForcingIdentifiersToUpperCase(true);
      try (Database database =
          new Database(new LoggingObject(swConnection.getName()), variables, databaseMeta)) {
        database.connect();
        for (String line : lines) {
          database.execStatement(line);
        }
      }

      // Also create some Lean connectors
      //
      LeanSqlConnector territoriesConnector =
          new LeanSqlConnector(
              "SteelWheels",
              "select coalesce(territory, 'UNKNOWN') as territory, count(*) as cnt "
                  + "from customer_w_ter "
                  + "group by territory "
                  + "order by 1 asc; ");
      LeanConnector territories = new LeanConnector("territories", territoriesConnector);
      metadataProvider.getSerializer(LeanConnector.class).save(territories);

      LeanSampleDataConnector sampleDataConnector = new LeanSampleDataConnector(100);
      LeanConnector sampleData = new LeanConnector("Sample Data", sampleDataConnector);
      metadataProvider.getSerializer(LeanConnector.class).save(sampleData);

    } catch (Exception e) {
      throw new HopException("Error saving connections", e);
    }
    log.logDetailed("Steel Wheels database created");
  }

  private void createTestThemes() throws HopException {

    LeanFont theme1Font = new LeanFont("Arial", "14", false, false);
    LeanFont title1Font = new LeanFont("Arial", "20", true, true);
    LeanColorRGB t1C1 = new LeanColorRGB(255, 200, 200);
    LeanColorRGB t1C2 = new LeanColorRGB(250, 210, 210);
    LeanColorRGB t1C3 = new LeanColorRGB(180, 180, 180);
    LeanColorRGB t1C4 = new LeanColorRGB(200, 100, 100);

    LeanTheme theme1 = new LeanTheme();
    theme1.setName("First Theme");
    theme1.setDefaultFont(theme1Font);
    theme1.setDefaultColor(t1C1);
    theme1.setBorderColor(t1C2);
    theme1.setTitleFont(title1Font);
    theme1.setBorderColor(t1C3);
    theme1.setTitleColor(t1C4);
    theme1.setColors(Arrays.asList(t1C1, t1C2, t1C3, t1C4));

    LeanFont theme2Font = new LeanFont("Arial", "14", false, false);
    LeanFont title2Font = new LeanFont("Arial", "20", true, true);
    LeanColorRGB t2C1 = new LeanColorRGB(200, 200, 244);
    LeanColorRGB t2C2 = new LeanColorRGB(200, 210, 255);
    LeanColorRGB t2C3 = new LeanColorRGB(180, 180, 250);
    LeanColorRGB t2C4 = new LeanColorRGB(100, 100, 250);

    LeanTheme theme2 = new LeanTheme();
    theme2.setName("Second Theme");
    theme2.setDefaultFont(theme2Font);
    theme2.setDefaultColor(t2C1);
    theme2.setBorderColor(t2C2);
    theme2.setTitleFont(title2Font);
    theme2.setBorderColor(t2C3);
    theme2.setTitleColor(t2C4);
    theme2.setColors(Arrays.asList(t2C1, t2C2, t2C3, t2C4));

    try {
      themeSerializer.save(theme1);
      themeSerializer.save(theme2);
    } catch (HopException e) {
      throw new HopException("Error saving test themes", e);
    }
  }

  public void storeRendering(IRendering rendering) {
    renderCache.put(rendering.getId(), rendering);
  }

  public IRendering getRendering(String id) {
    return renderCache.get(id);
  }

  public IRendering findRendering(String presentationName) {
    for (IRendering rendering : renderCache.values()) {
      if (presentationName.equals(rendering.getPresentationName())) {
        return rendering;
      }
    }
    return null;
  }

  /**
   * Gets leanUtil
   *
   * @return value of leanUtil
   */
  public static LeanUtil getLeanUtil() {
    return leanUtil;
  }

  /**
   * Sets leanUtil
   *
   * @param leanUtil value of leanUtil
   */
  public static void setLeanUtil(LeanUtil leanUtil) {
    LeanUtil.leanUtil = leanUtil;
  }

  /**
   * Gets loggingObject
   *
   * @return value of loggingObject
   */
  public LoggingObject getLoggingObject() {
    return loggingObject;
  }

  /**
   * Gets variables
   *
   * @return value of variables
   */
  public IVariables getVariables() {
    return variables;
  }

  /**
   * Gets metadataProvider
   *
   * @return value of metadataProvider
   */
  public IHopMetadataProvider getMetadataProvider() {
    return metadataProvider;
  }

  /**
   * Gets log
   *
   * @return value of log
   */
  public LogChannel getLog() {
    return log;
  }

  /**
   * Gets metadataPath
   *
   * @return value of metadataPath
   */
  public String getMetadataPath() {
    return metadataPath;
  }

  /**
   * Gets presentationSerializer
   *
   * @return value of presentationSerializer
   */
  public IHopMetadataSerializer<LeanPresentation> getPresentationSerializer() {
    return presentationSerializer;
  }

  /**
   * Sets presentationSerializer
   *
   * @param presentationSerializer value of presentationSerializer
   */
  public void setPresentationSerializer(
      IHopMetadataSerializer<LeanPresentation> presentationSerializer) {
    this.presentationSerializer = presentationSerializer;
  }

  /**
   * Gets dbConnSerializer
   *
   * @return value of dbConnSerializer
   */
  public IHopMetadataSerializer<LeanDatabaseConnection> getDbConnSerializer() {
    return dbConnSerializer;
  }

  /**
   * Sets dbConnSerializer
   *
   * @param dbConnSerializer value of dbConnSerializer
   */
  public void setDbConnSerializer(IHopMetadataSerializer<LeanDatabaseConnection> dbConnSerializer) {
    this.dbConnSerializer = dbConnSerializer;
  }

  /**
   * Gets themeSerializer
   *
   * @return value of themeSerializer
   */
  public IHopMetadataSerializer<LeanTheme> getThemeSerializer() {
    return themeSerializer;
  }

  /**
   * Sets themeSerializer
   *
   * @param themeSerializer value of themeSerializer
   */
  public void setThemeSerializer(IHopMetadataSerializer<LeanTheme> themeSerializer) {
    this.themeSerializer = themeSerializer;
  }

  /**
   * Gets metadataSerializer
   *
   * @return value of metadataSerializer
   */
  public IHopMetadataSerializer<IHopMetadata> getMetadataSerializer() {
    return metadataSerializer;
  }

  /**
   * Sets metadataSerializer
   *
   * @param metadataSerializer value of metadataSerializer
   */
  public void setMetadataSerializer(IHopMetadataSerializer<IHopMetadata> metadataSerializer) {
    this.metadataSerializer = metadataSerializer;
  }
}
