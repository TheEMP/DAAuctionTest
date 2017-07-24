package com.theemp.auctiontest;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by gunna on 7/21/2017.
 */
public class Main extends JavaPlugin implements Listener {
    FileConfiguration config = getConfig();

    private ArrayList<Auction> auctionList = new ArrayList<>();
    final String username="datestauction"; //Enter in your db username
    final String password="datestauction"; //Enter your password for the db
    final String url = "jdbc:mysql://db4free.net:3306/datestauction"; //Enter URL w/db name
    static Main instance;
    //Connection vars
    static Connection connection; //This is the variable we will use to connect to database

    public ArrayList getAuctionList(){
        return auctionList;
    }
    public static void main(String[] args){

    }
    private void setDBTables(){
        String sql = "CREATE TABLE IF NOT EXISTS auction_bids(id MEDIUMINT NOT NULL, bid MEDIUMINT NOT NULL, uuid VARCHAR(37) NOT NULL, price DOUBLE);";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String sql2 = "CREATE TABLE IF NOT EXISTS auction(id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY, uuid VARCHAR(37) NOT NULL, item VARCHAR(100), price DOUBLE);";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql2);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public Auction findAuctionByID(int id){
        for (Auction auction : auctionList) {
            if (auction.getDbID() == id)
                return auction;
        }
        throw new IllegalArgumentException();
    }

    public void createAuction(Player player, String item, int price, int time){
        try {
            String sql = "INSERT INTO auction(id, uuid,item, price) VALUES (0, ?, ?, ?);";
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, item);
            stmt.setInt(3, price);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            int auto_id = rs.getInt(1);
            rs.close();

            Auction auction = new Auction(player, time, item, price);
            auction.setDBID(auto_id);
            auctionList.add(auction);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    auction.end();
                    auctionList.remove(auction);
                }
            }, time*1000);
            stmt.close();
            Bukkit.broadcastMessage(player.getDisplayName()+" has created an auction for an "+item+" ($"+price+") that lasts "+time+ " seconds! Type /bid "+auto_id+" [price] to bid on this auction!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onEnable(){
        this.getCommand("auction").setExecutor(new AuctionCommand());
        this.getCommand("bid").setExecutor(new BidCommand());
        this.getCommand("auctionlist").setExecutor(new AuctionListCommand());

        getServer().getPluginManager().registerEvents(this, this);

        config.addDefault("minBet", 100);
        config.addDefault("timerDefault", 300);
        config.options().copyDefaults(true);
        saveConfig();

        try { //We use a try catch to avoid errors, hopefully we don't get any.
            Class.forName("com.mysql.jdbc.Driver"); //this accesses Driver in jdbc.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("jdbc driver unavailable!");
            return;
        }
        boolean connected = false;
        try { //Another try catch to get any SQL errors (for example connections errors)
            connection = DriverManager.getConnection(url,username,password);
            connected = true;
        } catch (SQLException e) { //catching errors)
            e.printStackTrace(); //prints out SQLException errors to the console (if any)
        }
        if (connected) setDBTables();
        instance = this;
    }

    @Override
    public void onDisable(){
        try { //using a try catch to catch connection errors (like wrong sql password...)
            if (connection != null && !connection.isClosed()){
                connection.close(); //closing the connection field variable.
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        for (Auction auction : auctionList) {
            if (player == auction.getCurBidPlayer()) {
                auction.resetBet();
                break;
            }
        }
    }
    public Player getPlayerByUuid(String uuid) {
        for(Player player : getServer().getOnlinePlayers())
            if (player.getUniqueId().toString().equals(uuid)) return player;

        throw new IllegalArgumentException();
    }
}
