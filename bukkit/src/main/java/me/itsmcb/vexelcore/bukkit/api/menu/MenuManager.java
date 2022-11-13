package me.itsmcb.vexelcore.bukkit.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class MenuManager {
    ArrayList<Menu> menus = new ArrayList<>();

    JavaPlugin instance;

    public MenuManager(JavaPlugin instance) {
        this.instance = instance;
    }

    public void addMenu(Menu menu) {
        this.menus.add(menu);
    }

    public Menu getMenu(String menuId) {
        return this.menus.stream().filter(menu -> menu.getMenuId().equalsIgnoreCase(menuId)).findFirst().orElse(null);
    }

    public void open(Player player, String menuId) {
        open(player, menuId, 1);
    }

    public void open(Player player, String menuId, int pageNumber) {
        // todo check if menu exists and if page exists
        Menu menu = getMenu(menuId);
        menu.openPage(player, pageNumber, instance);
    }
}
