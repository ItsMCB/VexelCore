package me.itsmcb.vexelcore.bukkit.api.menu;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class Menu {

    private String menuId;
    private HashMap<Integer, MenuPage> pages = new HashMap<>();
    private MenuManager menuManager;

    public Menu(String menuId, MenuManager menuManager) {
        this.menuId = menuId;
        this.menuManager = menuManager;
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
        pages.get(pageNumber).open(player, menuManager);
    }
}
