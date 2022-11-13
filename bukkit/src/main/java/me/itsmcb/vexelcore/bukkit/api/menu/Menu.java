package me.itsmcb.vexelcore.bukkit.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class Menu {

    String menuId;
    HashMap<Integer, MenuPage> pages = new HashMap<>();

    public Menu(String menuId) {
        this.menuId = menuId;
    }

    public String getMenuId() {
        return menuId;
    }

    public void addPage(int pageNumber, MenuPage page) {
        pages.put(pageNumber, page);
    }

    public MenuPage getPage(int pageNumber) {
        return pages.get(pageNumber);
    }

    public void openPage(Player player, int pageNumber, JavaPlugin instance) {
        pages.get(pageNumber).open(player, instance);
    }
}
