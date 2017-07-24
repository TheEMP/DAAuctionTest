package com.theemp.auctiontest;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by gunna on 7/21/2017.
 */
public class Auction {
    private Player owner;
    private Player curBidPlayer;
    private int time;
    private int startPrice;
    private int dbID;
    private String item;
    private int curPrice;
    private int curBid;
    private int curBidNum = 1;
    private Connection connection;

    public int getCurPrice(){
        return curPrice;
    }

    public int getCurBid(){
        return curBid;
    }

    public Player getCurBidPlayer(){
        return curBidPlayer;
    }

    public Player getOwner(){
        return owner;
    }

    public int getCurBidNum() {
        return curBidNum;
    }

    public String getItem(){
        return item;
    }

    public boolean canBid(Player player, int bid) {
        //if (curBidPlayer == player) return false; // Don't let players bid on the same item if twice
        if ((curBid - bid) >= Main.instance.config.getInt("minBet")) return false; // Don't let the player bid if their bid is not more then the max amount
        //Price Checks for money if we had an economy plugin
        return true;
    }

    public int getDbID(){
        return dbID;
    }

    public void setDBID(int id){
        dbID = id;
    }

    private void saveBid(Player player, int bid){
        String sql = "INSERT INTO auction_bids(id, bid, uuid, price) VALUES (?, ?, ?, ?);";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1,dbID);
            stmt.setInt(2, curBidNum);
            stmt.setString(3, player.getUniqueId().toString());
            stmt.setInt(4, bid);

            stmt.executeUpdate();
            curBidNum++;
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void bid(Player player, int bid){
        if (curBidPlayer != null) {
            curBidPlayer.sendMessage("Your bid on " + owner.getDisplayName() + "'s a " + item + " has been beaten! Your holding funds have been returned to you $" + curPrice);
            //curBidPlayer.giveMoney(curPrice); // Return money to the last bidder with curPrice
        }
        curBidPlayer = player;
        curPrice = curBid; // Swap the current buy price to the old bid
        curBid = bid; // Update the bid price to match what the player put
        saveBid(player, bid);
        player.sendMessage("Your bid on "+owner.getDisplayName()+"'s a "+item+" has placed! The amount of $"+curPrice+" has been placed on hold.");
    }
    public void printAuctionBids(Player player){
        String sql = "SELECT * FROM auction_bids WHERE id=? ORDER BY bid ASC;";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, dbID);
            ResultSet results = stmt.executeQuery();
            while (results.next()){
                try {
                    Player ply = Main.instance.getPlayerByUuid(results.getString("uuid"));
                    int bidID = results.getInt("bid");
                    int price = results.getInt("price");
                    player.sendMessage(bidID+" : "+ply.getDisplayName()+" - "+price);
                }
                catch (IllegalArgumentException i){
                    int bidID = results.getInt("bid");
                    int price = results.getInt("price");
                    player.sendMessage(bidID+" : Offline Player - "+price);
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void resetBet(){
        String sql = "SELECT * FROM auction_bids WHERE id=? AND bid =?;";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, dbID);
            stmt.setInt(2, curBidNum-1);
            ResultSet results = stmt.executeQuery();
            try {
                Player player = Main.instance.getPlayerByUuid(results.getString("uuid"));
                int price = results.getInt("price");
                curPrice = price;
                curBid = price;
                curBidPlayer = player;
                saveBid(player, price);
            }
            catch (IllegalArgumentException i){
                i.printStackTrace();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void end(){
        curBidPlayer.sendMessage("You have won your bid on "+item);
        //Give items
        Main.instance.config.set("auction_"+dbID, curBidPlayer.getDisplayName()+" : "+ item);
    }

    public Auction(Player ply, int startTime, String plyItem, int price){
        owner = ply;
        time = startTime;
        item = plyItem;
        startPrice = price;
        curBid = price;
        curPrice = price;
        connection = Main.connection;

    }
}
