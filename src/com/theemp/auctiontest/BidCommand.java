package com.theemp.auctiontest;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by gunna on 7/23/2017.
 */
public class BidCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) return false;

        if (sender instanceof Player) {
            Player player = (Player) sender;
            try {
                Auction auction = Main.instance.findAuctionByID(Integer.parseInt(args[0]));
                int bid = Integer.parseInt(args[1]);
                if (!auction.canBid(player, bid)) {
                    player.sendMessage("You can't bit like that!");
                    return false;
                }
                auction.bid(player,Integer.parseInt(args[1]));
            }
            catch (IllegalArgumentException i){
                player.sendMessage("Auction not found!");
                return false;
            }
        }
        return true;
    }
}
