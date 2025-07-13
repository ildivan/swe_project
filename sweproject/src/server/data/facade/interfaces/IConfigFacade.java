package server.data.facade.interfaces;

import server.utils.ConfigType;
import server.utils.Configs;

public interface IConfigFacade {
    public void initializeConfig();
    public Configs getConfig(ConfigType configType);
    public boolean save(Configs configs, ConfigType configType);
    public boolean checkIfFirstMonthlyPlan(ConfigType configType);
    public void firstTimeConfigurationServerConfig();

}
