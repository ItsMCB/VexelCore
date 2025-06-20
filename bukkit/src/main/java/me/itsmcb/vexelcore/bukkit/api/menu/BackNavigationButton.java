package me.itsmcb.vexelcore.bukkit.api.menu;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import me.itsmcb.vexelcore.bukkit.api.utils.SkullBuilderUtil;
import me.itsmcb.vexelcore.common.api.HeadTexture;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BackNavigationButton extends MenuButton {

    private PaginatedMenu paginatedMenu;

    public BackNavigationButton(PaginatedMenu associatedMenu) {
        super(Material.BLACK_STAINED_GLASS);
        this.paginatedMenu = associatedMenu;
    }

    public void updatePage() {
        if (paginatedMenu.pageCanGoBack()) {
            this.setItemStack(new SkullBuilderUtil(HeadTexture.GRAY_ARROW_LEFT.getTexture()).get());
            this.name("&r&d&lPrevious Page");
            this.click(e -> paginatedMenu.pageGoBack());
            this.setLore(new BukkitMsgBuilder("&7Page&8: &7"+paginatedMenu.getPage()+"&8/&7"+paginatedMenu.getTotalPages()).get());
        } else {
            this.setItemStack(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)).name("&7");
            this.click(null);
            this.resetLore();
        }
    }

    @Override
    public MenuButton refresh() {
        updatePage();
        return super.refresh();
    }
}
