package me.itsmcb.vexelcore.bukkit.api.menu;

import me.itsmcb.vexelcore.bukkit.api.text.BukkitMsgBuilder;
import me.itsmcb.vexelcore.bukkit.api.utils.SkullBuilderUtil;
import me.itsmcb.vexelcore.common.api.HeadTexture;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ForwardNavigationButton extends MenuButton {

    private PaginatedMenu paginatedMenu;

    public ForwardNavigationButton(@NotNull String texture, PaginatedMenu paginatedMenu) {
        super(texture);
        this.paginatedMenu = paginatedMenu;
    }

    @Override
    public MenuButton refresh() {
        if (!paginatedMenu.isLastPage()) {
            this.setItemStack(new SkullBuilderUtil(HeadTexture.GRAY_ARROW_RIGHHT.getTexture()).get());
            this.name("&r&d&lNext Page");
            this.click(e -> paginatedMenu.pageGoForward());
            this.setLore(new BukkitMsgBuilder("&7Page&8: &7"+paginatedMenu.getPage()+"&8/&7"+paginatedMenu.getTotalPages()).get());
        } else {
            this.setItemStack(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)).name("&7");
            this.click(null);
            this.resetLore();
        }
        return super.refresh();
    }
}
