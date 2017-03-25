package me.c0wg0d;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class NameSync extends JavaPlugin {

	private File playerDataFolder;
	private File cachedUUIDFile;
	private static NameSync plugin;
	public Logger logger;

    private SettingsManager settings = SettingsManager.getInstance();

	//Connection vars
	static Connection connection; //This is the variable we will use to connect to database

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable()
	{
		plugin = this;
        settings.init(plugin);
		this.logger = this.getLogger();
		this.playerDataFolder = new File(Bukkit.getWorlds().get(0).getWorldFolder().getPath(), "playerdata");
		this.cachedUUIDFile = new File(this.getDataFolder(), "uuids.dat");
		Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
		NameSyncApi.loadUUIDS(this, this.cachedUUIDFile, this.playerDataFolder.listFiles());
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable()
		{
			@Override
			public void run()
			{
				logger.log(Level.INFO, "UUIDS saved to file!");
				NameSyncApi.saveData(cachedUUIDFile);
			}
		}, 1L, 72000L);
	}
	
	@Override
	public void onDisable()
	{
		NameSyncApi.saveData(this.cachedUUIDFile);

		try { //using a try catch to catch connection errors (like wrong sql password...)
			if(connection!=null && !connection.isClosed()){ //checking if connection isn't null to
				//avoid recieving a nullpointer
				connection.close(); //closing the connection field variable.
			}
		} catch(Exception e){
			e.printStackTrace();

		}
	}
	
	protected static NameSync getInstance()
	{
		return plugin;
	}
}