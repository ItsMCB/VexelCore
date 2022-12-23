package me.itsmcb.vexelcore.bukkit.api.menu;

import org.bukkit.entity.Player;

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

    public HashMap<Integer, MenuPage> getPages() {
        return pages;
    }

    public void openPage(Player player, int pageNumber) {
        pages.get(pageNumber).open(player);
    }
}
