package org.lean.rest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hop.core.database.Database;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.logging.LoggingObject;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.metadata.api.IHopMetadataSerializer;
import org.apache.hop.metadata.serializer.memory.MemoryMetadataProvider;
import org.lean.core.LeanColorRGB;
import org.lean.core.LeanDatabaseConnection;
import org.lean.core.LeanEnvironment;
import org.lean.core.LeanFont;
import org.lean.core.exception.LeanException;
import org.lean.presentation.LeanPresentation;
import org.lean.presentation.datacontext.IDataContext;
import org.lean.presentation.datacontext.PresentationDataContext;
import org.lean.presentation.layout.LeanLayoutResults;
import org.lean.presentation.layout.LeanRenderPage;
import org.lean.presentation.theme.LeanTheme;
import org.lean.render.IRenderContext;
import org.lean.render.context.PresentationRenderContext;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class LeanUtil {

    private static LeanUtil leanUtil;
    private LoggingObject loggingObject;
    private IVariables variables;
    private IHopMetadataProvider metadataProvider;
    private IHopMetadataSerializer<LeanPresentation> presentationSerializer;
    private IHopMetadataSerializer<LeanDatabaseConnection> dbConnSerializer;
    private IHopMetadataSerializer<LeanTheme> themeSerializer;
    private PluginRegistry registry;
    private LogChannel log;

    private List<String> presentationNames;

    private String presentationsPath;

    private List<LeanPresentation> presentationList;
    private Map<String, List<LeanRenderPage>> presentationRenderPages;


    public LeanUtil(){
        presentationList = new ArrayList<LeanPresentation>();
        presentationRenderPages = new HashMap<String, List<LeanRenderPage>>();

        loggingObject = new LoggingObject("Lean Rest");
        log = new LogChannel("Lean Rest Server");
        try{
            LeanEnvironment.init();
        }catch(LeanException e){
            log.logError("Could not initialize the lean environment", e);
        }
        Properties props = new Properties();
        String propertyPath;
        InputStream inputStream;
        try {
            if(StringUtils.isEmpty(System.getProperty("CONFIG_PATH"))){
                propertyPath = "/config";
            }else{
                propertyPath = System.getProperty("CONFIG_PATH");
            }
            inputStream = new FileInputStream(propertyPath + "/leanrest.properties");
            props.load(inputStream);
        }catch(IOException e){
            log.logError("Error initializing Lean Rest: ", e);
        }
        presentationsPath = props.getProperty("presentations.path");

        metadataProvider = new MemoryMetadataProvider();
        variables = Variables.getADefaultVariableSpace();

        presentationNames = new ArrayList<>();

        try {
            registry = PluginRegistry.getInstance();
            registry.init();
            LeanEnvironment.init();
            presentationSerializer = metadataProvider.getSerializer(LeanPresentation.class);
            dbConnSerializer = metadataProvider.getSerializer(LeanDatabaseConnection.class);
            themeSerializer = metadataProvider.getSerializer(LeanTheme.class);

            // prepare sample presentations
            createConnections();
            loadSamples();
            createTestThemes();

        }catch(LeanException e){
            log.logError("Error while starting Lean server: ", e);
        }catch(HopException e){
            log.logError("Error creating presentation serializer: ", e);
        }
    }

    public static LeanUtil getInstance(){
        if(leanUtil == null){
            leanUtil = new LeanUtil();
        }
        return leanUtil;
    }

    public IHopMetadataProvider getMetadataProvider(){
        return metadataProvider;
    }

    public List<String> getPresentationNames() throws HopException, LeanException {
        return presentationNames;
    }

    private List<LeanRenderPage> renderPresentation(String presentationName) throws LeanException, HopException {
        LeanPresentation presentation = presentationSerializer.load(presentationName);
        IRenderContext renderContext = new PresentationRenderContext(presentation);
        IDataContext dataContext = new PresentationDataContext(presentation, metadataProvider);
        LeanLayoutResults layout = presentation.doLayout(loggingObject, renderContext, metadataProvider, Collections.emptyList());
        presentation.render(layout, metadataProvider);
        List<LeanRenderPage> renderPages = layout.getRenderPages();
        return renderPages;
    }
    public String getPresentationSVG(String presentationName, int pageNumber) throws HopException, LeanException {
        log.logBasic("Loading presentation " + presentationName);

        List<LeanRenderPage> renderPages = renderPresentation(presentationName);
        return renderPages.get(pageNumber).getSvgXml();

    }


    public List<String> getComponentsAt(String presentationName, int pageNb, String posXY) throws HopException, LeanException{
        int posX = Integer.valueOf(posXY.split(",")[0]);
        int posY = Integer.valueOf(posXY.split(",")[1]);

        List<LeanRenderPage> renderPages = renderPresentation(presentationName);
        LeanRenderPage renderPage = renderPages.get(pageNb);
        List<String> componentNameList = renderPage.lookupComponentName(posX, posY);

        return componentNameList;
    }


    private void loadSamples() throws HopException{
        File presDir = new File(presentationsPath);
        log.logDetailed("presDir: " + presDir.getAbsolutePath());
        File[] presFiles = presDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".json");
            }
        });

        if(presFiles != null){
            log.logBasic("We found " + presFiles.length + " in " + presDir);
            for(File presFile : presFiles) {
                try {
                    FileInputStream inputStream = new FileInputStream(presFile);
                    String presJSON = IOUtils.toString(inputStream);
                    LeanPresentation presentation= new LeanPresentation().fromJsonString(presJSON);
                    if(!presentationList.contains(presentation)){
                        presentationList.add(presentation);
                    }

                    if (StringUtils.isEmpty(presentation.getDefaultThemeName())) {
                        LeanTheme theme = LeanTheme.getDefault();
                        if (presentation.getThemes().size() > 0) {
                            if (StringUtils.isEmpty(presentation.getThemes().get(0).getName())) {
                                presentation.getThemes().remove(0);
                            }
                        }
                        presentation.getThemes().add(theme);
                        presentation.setDefaultThemeName(theme.getName());
                    }

                    presentationSerializer.save(presentation);
                    presentationNames.add(presentation.getName());
                }catch(Exception e){
                    log.logError("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }else {
            log.logBasic("No presentation files found at location " + presentationsPath);
        }

    }

    private void createConnections() throws HopException {
        String CONNECTOR_STEEL_WHEELS_NAME = "SteelWheels";
        LeanDatabaseConnection connection2 = new LeanDatabaseConnection("logging", "POSTGRESQL", "localhost", "5432",
                "logging", "postgres", "postgres");
        String h2DatabaseName = System.getProperty("java.io.tmpdir") + File.separator + CONNECTOR_STEEL_WHEELS_NAME;

        LeanDatabaseConnection swConnection = new LeanDatabaseConnection(CONNECTOR_STEEL_WHEELS_NAME,  "H2", null, null, h2DatabaseName , null, null);

        try {
            dbConnSerializer.save(connection2);
            dbConnSerializer.save(swConnection);
        } catch (HopException e) {
            log.logError("Error saving dummy connections: " + e.getMessage());
        }


        try {
            // Delete old database
            //
            File[] files = new File( System.getProperty( "java.io.tmpdir" ) ).listFiles(
                    new FileFilter() {
                        @Override public boolean accept( File pathname ) {
                            return pathname.toString().endsWith( ".db" ) && pathname.toString().contains( CONNECTOR_STEEL_WHEELS_NAME );
                        }
                    } );
            for (File file : files) {
                FileUtils.forceDelete(file);
            }

            // Read the script
            //
            List<String> lines = Files.readAllLines( Paths.get(getClass().getClassLoader().getResource("steelwheels.script" ).getPath()));

            DatabaseMeta databaseMeta = swConnection.createDatabaseMeta();
            databaseMeta.setForcingIdentifiersToUpperCase(true);
            Database database = new Database( new LoggingObject( swConnection.getName() ), variables, databaseMeta );
            try {
                database.connect();

                for ( String line : lines ) {
                    database.execStatement( line );
                }

            } finally {
                database.disconnect();
            }
        }catch(Exception e) {
            log.logError("Error: " + e.getMessage());
        }
        log.logDetailed("Steel Wheels database created");
    }

    private void createTestThemes() throws HopException {

        LeanFont theme1Font = new LeanFont("Arial", "14", false, false);
        LeanFont title1Font = new LeanFont("Arial", "20", true, true);
        LeanColorRGB t1C1 = new LeanColorRGB(255, 200, 200);
        LeanColorRGB t1C2 = new LeanColorRGB(250, 210, 210);
        LeanColorRGB t1C3 = new LeanColorRGB(180,180,180);
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
        LeanColorRGB t2C3 = new LeanColorRGB(180,180,250);
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

        try{
            themeSerializer.save(theme1);
            themeSerializer.save(theme2);
        }catch(HopException e){
            log.logError("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
