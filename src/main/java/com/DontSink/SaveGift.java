package com.DontSink;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.util.io.BukkitObjectOutputStream;
public class SaveGift implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        
        //saveGift gift1
        //args = [save,gift1]
        String a1 = args[0];
        Player p = (Player) commandSender;
        saveGift(p, a1);
        return false;
    }

    public void saveGift(Player player, String a1) {
        try {
            // 获取玩家的UUID
            UUID playerUUID = player.getUniqueId();
            // 获取玩家的物品栏
            Inventory inventory = player.getInventory();
            // 获取物品栏中的所有物品
            ItemStack[] items = inventory.getContents();
            //调用从主类来的插件实例
            Gift plugin = Gift.getInstance();
            // 创建一个文件对象，文件名为玩家的UUID，扩展名为.dat
            File file = new File(plugin.getDataFolder(), a1 + "_Gift.dat");
            // 如果文件不存在，则创建文件
            if (!file.exists()) {
                file.getParentFile().mkdirs(); // 创建父目录
                file.createNewFile(); // 创建文件
            }
            // 使用文件输出流和对象输出流将物品栏中的物品写入文件
            try (FileOutputStream fos = new FileOutputStream(file);
                 BukkitObjectOutputStream boos = new BukkitObjectOutputStream(fos)) {
                boos.writeObject(items); // 将物品数组写入文件
            }
            // 向玩家发送消息，通知物品栏已保存
            player.sendMessage("物品栏已保存在" + a1 + "中");
        } catch (IOException e) {
            // 如果发生异常，向玩家发送错误消息，并打印堆栈跟踪
            player.sendMessage("保存物品栏时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

