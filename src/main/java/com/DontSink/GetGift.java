package com.DontSink;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.bukkit.Bukkit.getLogger;

public class GetGift implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        //gift save gift1
        //args = [save,gift1]
        String a1 = args[0];
        Player p = (Player) commandSender;
        getGift(p, a1);
        return false;
    }

    //玩家领取礼包
    public void getGift(Player player, String a2) {
        try {
            // 获取玩家的UUID
            UUID playerUUID = player.getUniqueId();
            //调用从主类来的插件实例
            Gift plugin = Gift.getInstance();
            // 创建一个文件对象，扩展名为.dat 用于查找礼包文件
            File file = new File(plugin.getDataFolder(), a2 + "_Gift.dat");
            // 如果文件不存在，则向玩家发送错误消息
            if (!file.exists()) {
                player.sendMessage("不存在" + a2 + "礼包");
                return;
            }

            // 读取礼包配置文件以获取时间限制
            int timeLimit = 0; // 时间限制
            File configFile = new File("plugins/Gift/GiftConfig.txt");
            if (configFile.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith(a2 + ".time_")) {
                            timeLimit = Integer.parseInt(line.split("_")[1]);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 调用getGiftDetect方法进行时间和次数的判断
            int detectResult = getGiftDetect(player, a2);
            if (detectResult != 0) {
                // 根据返回值向玩家发送相应的错误消息
                if (detectResult == 1) {
                    player.sendMessage("您已达到领取次数限制");
                } else if (detectResult == 2) {
                    // 获取最后领取时间
                    Date lastGiftDate = null;
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    File logFile = new File("plugins/Gift/PlayerGiftLog.txt");
                    if (logFile.exists()) {
                        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
                            String line;
                            while ((line = br.readLine()) != null) {
                                if (line.contains("玩家" + playerUUID) && line.contains("领取了" + a2 + "礼包")) {
                                    String[] parts = line.split("在");
                                    if (parts.length > 1) {
                                        String dateString = parts[1].split("领取了")[0];
                                        lastGiftDate = formatter.parse(dateString);
                                    }
                                }
                            }
                        } catch (IOException | ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    // 计算时间差
                    if (lastGiftDate != null) {
                        Date currentDate = new Date();
                        long diff = currentDate.getTime() - lastGiftDate.getTime();
                        long diffMinutes = diff / (60 * 1000); // 计算时间差（分钟）
                        player.sendMessage("您在冷却时间内，无法领取礼包，剩余冷却时间: " + (timeLimit - diffMinutes) + " 分钟");
                    }
                }
                return; // 结束方法
            }

            // 测试
            final String ANSI_GREEN = "\u001B[36m";
            getLogger().info(ANSI_GREEN + getGiftDetect(player, a2));

            // 使用文件输入流和对象输入流从文件中读取物品栏中的物品
            try (FileInputStream fis = new FileInputStream(file);
                 BukkitObjectInputStream bois = new BukkitObjectInputStream(fis)) {
                ItemStack[] items = (ItemStack[]) bois.readObject(); // 从文件中读取物品数组
                // 获取玩家的物品栏
                Inventory inventory = player.getInventory();
                // 检查物品栏是否有足够的空位
                for (ItemStack item : items) {
                    if (item != null) {
                        if (inventory.firstEmpty() == -1) {
                            // 如果没有空位，将物品掉落在玩家脚下
                            player.getWorld().dropItemNaturally(player.getLocation(), item);
                        } else {
                            // 如果有空位，将物品放入物品栏
                            inventory.addItem(item);
                        }
                    }
                }
            }

            playerGiftLog(player, a2);
            // 向玩家发送消息，通知物品栏已加载
            player.sendMessage("成功领取" + a2 + "礼包");
        } catch (IOException | ClassNotFoundException e) {
            // 如果发生异常，向玩家发送错误消息，并打印堆栈跟踪
            player.sendMessage("领取礼包错误" + e.getMessage());
            e.printStackTrace();
        }
    }

    //用于查找玩家是否能领取礼包
    public int getGiftDetect(Player p, String giftName) {
        final String ANSI_GREEN = "\u001B[36m";
        UUID playerUUID = p.getUniqueId();
        Date currentDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        int giftCount = 0;
        Date lastGiftDate = null;
        int timeLimit = 0; // 时间限制
        int maxCount = 0; // 最大领取次数

        getLogger().info(ANSI_GREEN + "开始运行getGiftDetect方法");

        // 遍历领取日志
        File logFile = new File("plugins/Gift/PlayerGiftLog.txt");
        if (logFile.exists()) {
            getLogger().info(ANSI_GREEN + "开始遍历领取log");
            try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    getLogger().info(ANSI_GREEN + "读取日志行: " + line);
                    if (line.contains("玩家" + playerUUID) && line.contains("领取了" + giftName + "礼包")) {
                        giftCount++;
                        String[] parts = line.split("在");
                        if (parts.length > 1) {
                            String dateString = parts[1].split("领取了")[0];
                            Date date = formatter.parse(dateString);
                            if (lastGiftDate == null || date.after(lastGiftDate)) {
                                lastGiftDate = date;
                            }
                        }
                    }
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
            getLogger().info(ANSI_GREEN + "结束遍历领取log");
        } else {
            getLogger().info(ANSI_GREEN + "领取日志文件不存在");
        }

        // 读取礼包配置文件
        File configFile = new File("plugins/Gift/GiftConfig.txt");
        if (configFile.exists()) {
            getLogger().info(ANSI_GREEN + "开始读取礼包配置文件");
            try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(giftName + ".time_")) {
                        timeLimit = Integer.parseInt(line.split("_")[1]);
                        getLogger().info(ANSI_GREEN + "读取时间限制: " + timeLimit + " 分钟");
                    } else if (line.startsWith(giftName + ".num_")) {
                        maxCount = Integer.parseInt(line.split("_")[1]);
                        getLogger().info(ANSI_GREEN + "读取最大领取次数: " + maxCount);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            getLogger().info(ANSI_GREEN + "礼包配置文件不存在");
        }

        getLogger().info(ANSI_GREEN + "领取次数计数: " + giftCount + " 最大领取次数: " + maxCount + " 最后礼物领取时间: " + lastGiftDate);

        // 检查领取次数
        if (giftCount >= maxCount) {
            getLogger().info(ANSI_GREEN + "结束比较领取次数");
            return 1; // 次数不符合
        }

        // 检查时间限制
        if (lastGiftDate != null) {
            long diff = currentDate.getTime() - lastGiftDate.getTime();
            long diffMinutes = diff / (60 * 1000);
            getLogger().info(ANSI_GREEN + "时间差: " + diffMinutes + " 分钟, 时间限制: " + timeLimit + " 分钟");
            if (diffMinutes < timeLimit) {
                getLogger().info(ANSI_GREEN + "结束比较领取cd");
                return 2; // 时间不符合
            }
        }

        getLogger().info(ANSI_GREEN + "成功判定getGiftDetect方法");
        return 0; // 符合领取条件
    }

    //用于创建领取记录文件及记录玩家领取的事件时间
    public void playerGiftLog(Player p, String giftName) {
        // 获取玩家的UUID
        UUID playerUUID = p.getUniqueId();
        //创建一个文件对象
        File file = new File("plugins/Gift/PlayerGiftLog.txt");
        // 确保父目录存在
        file.getParentFile().mkdirs();
        // 如果没有则创建文件
        if (!file.exists()) {
//            file.getParentFile().mkdirs(); // 创建父目录
            try {
                file.createNewFile(); // 创建文件
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        //获取当前时间
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String formattedDate = formatter.format(date);
        //在文件末位添加一条内容为 玩家uuid 在 日期 领取了 礼包名 礼包
        String info = "玩家" + playerUUID + "在" + formattedDate + "领取了" + giftName + "礼包";
        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(info); // 将信息另起一行存入txt文件
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}