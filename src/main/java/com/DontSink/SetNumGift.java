package com.DontSink;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class SetNumGift implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        String a1 = args[0];
        String a2 = args[1];
        Player p = (Player) commandSender;
        setTimeGift(p, a1, a2);
        return false;
    }

    public void setTimeGift(Player p, String a1, String a2) {
        try {
            // 获取插件实例
            Gift plugin = Gift.getInstance();
            // 创建一个文件对象
            File file = new File(plugin.getDataFolder(), "GiftConfig.txt");
            // 如果文件不存在，则创建文件
            if (!file.exists()) {
                file.getParentFile().mkdirs(); // 创建父目录
                file.createNewFile(); // 创建文件
            }
            // 读取文件内容
            List<String> lines = Files.readAllLines(file.toPath());
            String newEntry = a1 + ".num_" + a2;
            boolean replaced = false;

            // 检查是否存在相同的条目并替换
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith(a1 + ".num_")) {
                    lines.set(i, newEntry);
                    replaced = true;
                    break;
                }
            }
            // 如果没有替换，则添加新条目
            if (!replaced) {
                lines.add(newEntry);
            }

            // 将更新后的内容写回文件
            Files.write(file.toPath(), lines, StandardOpenOption.TRUNCATE_EXISTING);

            // 向玩家发送消息，通知内容已保存
            p.sendMessage("已设定" + a1 + "礼包的领取次数为"+a2+"次");
        } catch (IOException e) {
            // 如果发生异常，向玩家发送错误消息，并打印堆栈跟踪
            p.sendMessage("设定领取次数出错: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
