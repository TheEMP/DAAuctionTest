package com.theemp.auctiontest;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by gunna on 7/22/2017.
 */
public class AuctionListCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0) {
                try {
                    Auction auction = Main.instance.findAuctionByID(Integer.parseInt(args[0]));
                    player.sendMessage("Current Bids for "+auction.getOwner().getDisplayName());
                    auction.printAuctionBids(player);
                }
                catch (IllegalArgumentException i){
                    return false;
                }
                return true;
            }
            player.sendMessage("Current Auctions");
            ArrayList<Auction> auctionList = Main.instance.getAuctionList();
            for (Auction auction : auctionList) {
                player.sendMessage(auction.getOwner().getDisplayName() + "'s auction for " + auction.getItem() + " Current Bids #"+auction.getCurBidNum()+" Current Price $"+auction.getCurPrice());
            }
        }
        return true;
    }
}
