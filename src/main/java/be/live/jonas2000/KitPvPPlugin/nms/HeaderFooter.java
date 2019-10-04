package be.live.jonas2000.KitPvPPlugin.nms;

import net.minecraft.server.v1_13_R2.IChatBaseComponent;
import net.minecraft.server.v1_13_R2.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_13_R2.PacketPlayOutPlayerListHeaderFooter;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HeaderFooter implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        List onlinePlayers = new ArrayList<>(Arrays.asList(Bukkit.getOnlinePlayers()));
        int amountOfPlayersOnline = onlinePlayers.size();

        IChatBaseComponent header = ChatSerializer.a("{\"text\":\"" + "Welkom op de DusDavidGames server!" + "\"}");
        IChatBaseComponent footer = ChatSerializer.a("{\"text\":\"" + "Play.DusDavidGames.nl" + "\"}");

        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();

        try{
            Field footerField = packet.getClass().getDeclaredField("footer");
            footerField.setAccessible(true);
            footerField.set(packet,footer);

            Field headerField = packet.getClass().getDeclaredField("header");
            headerField.setAccessible(true);
            headerField.set(packet,header);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ((CraftPlayer) e.getPlayer()).getHandle().playerConnection.sendPacket(packet);
    }
}
