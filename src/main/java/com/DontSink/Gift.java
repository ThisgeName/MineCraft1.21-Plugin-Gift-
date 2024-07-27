package com.DontSink;


import org.bukkit.plugin.java.JavaPlugin;

public class Gift extends JavaPlugin {
    //创建主类实例用于副类调用 因为getDataFolder()方法只能用于主类
    private static Gift instance;
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        final String ANSI_GREEN = "\u001B[36m";
        this.getLogger().info(ANSI_GREEN+"Gift is enabled");
        this.getCommand("saveGift").setExecutor(new SaveGift());
        this.getCommand("getGift").setExecutor(new GetGift());
        this.getCommand("setTimeGift").setExecutor(new SetTimeGift());
        this.getCommand("setNumGift").setExecutor(new SetNumGift());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        final String ANSI_GREEN = "\u001B[36m";
        this.getLogger().info(ANSI_GREEN+"Gift is disabled");
    }
    //实现单例模式（Singleton Pattern），确保在整个插件中只有一个Gift实例，并且可以从其他类中访问这个实例。
    public static Gift getInstance() {
        return instance;
    }
}
