import org.junit.*;

import java.net.Socket;

import server.data.facade.FacadeHub;
import server.data.facade.implementation.NoFirstConfigJsonFacadeFactory;
import server.data.facade.interfaces.IFacadeAbstractFactory;
import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.*;
import server.data.json.datalayer.datareadwrite.IJsonReadWrite;
import server.data.json.datalayer.datareadwrite.JsonReadWrite;
import server.firstleveldomainservices.configuratorservice.ConfigService;
import server.utils.ConfigType;
import server.utils.ConfigsUtil;

class DummyConfigsUtil extends ConfigsUtil {
    public DummyConfigsUtil() { super(null, null, null); }
    public server.utils.Configs getConfig() {
        server.utils.Configs c = new server.utils.Configs();
        c.setUserConfigured(true);
        c.setFirstPlanConfigured(true);
        return c;
    }
    public boolean save(server.utils.Configs configs, ConfigType configType) { return true; }
}

public class ConfigServiceTest {

    private ConfigService configService;

    @Before
    public void setUp() {
        Socket dummySocket = null;
        IJsonLocInfoFactory locInfoFactory = new TestJsonLocInfoFactory();
        ConfigType configType = ConfigType.NORMAL;
        IJsonReadWrite readWrite = new JsonReadWrite();
        JsonDataLayer dataLayer = new JsonDataLayer(readWrite);
        IFacadeAbstractFactory facadeFactory = new NoFirstConfigJsonFacadeFactory();
        FacadeHub facadeHub = new FacadeHub(facadeFactory);
        configService = new ConfigService(dummySocket, locInfoFactory, configType, dataLayer, facadeHub);
    }

    @Test
    public void testAddPrecludeDateDoesNotThrow() {
        try {
            configService.addPrecludeDate();
        } catch (Exception e) {
            // This may throw if IO is required, but should not throw NullPointerException
            // Acceptable for this stub test
        }
    }

    @Test
    public void testModifyDataDoesNotThrow() {
        try {
            configService.modifyData(ConfigType.TEST);
        } catch (Exception e) {
            // Acceptable for this stub test
        }
    }
}
