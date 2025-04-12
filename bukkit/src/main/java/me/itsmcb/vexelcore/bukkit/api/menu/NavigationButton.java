package me.itsmcb.vexelcore.bukkit.api.menu;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class NavigationButton extends MenuButton {

    private PaginatedMenu paginatedMenu;

    public NavigationButton(@NotNull Material material, PaginatedMenu paginatedMenu) {
        super(material);
        this.paginatedMenu = paginatedMenu;
    }

    public NavigationButton(@NotNull String texture, PaginatedMenu paginatedMenu) {
        super(texture);
        this.paginatedMenu = paginatedMenu;
    }

    @Override
    public MenuButton refresh() {
        setLore(new BukkitMsgBuilder("&7Page&8: &7"+paginatedMenu.getPage()+"&8/&7"+paginatedMenu.getTotalPages()).get());
        return super.refresh();
    }
}
