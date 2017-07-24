package com.theemp.auctiontest;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by gunna on 7/22/2017.
 */
public class AuctionCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 2) return false;

        if (sender instanceof Player) {
            Player player = (Player) sender;
            int time = Integer.parseInt(args[2]);
            if (time == 0){
                time = Main.instance.config.getInt("timerDefault");
            }
            Main.instance.createAuction(player, args[0],Integer.parseInt(args[1]),time);
        }
        return true;
    }
}
