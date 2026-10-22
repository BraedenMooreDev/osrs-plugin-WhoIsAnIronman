package org.BraedenMooreDev.models;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.game.ChatIconManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Slf4j
@Getter
public enum AccountType
{
    NORMAL("Normal", 0, "", ""),
    IRONMAN("Ironman", 2, "ironman.png", "ironman_x.png"),
    ULTIMATE_IRONMAN("Ultimate Ironman", 3, "ultimate_ironman.png", "ultimate_ironman_x.png"),
    HARDCORE_IRONMAN("Hardcore Ironman", 10, "hardcore_ironman.png", "hardcore_ironman_x.png");

    private final String displayName;
    private final int defaultIcon;
    private final String iconPath;
    private final String lostIconPath;

    private int icon;
    private int lostIcon;
    private BufferedImage iconImg;
    private BufferedImage lostIconImg;

    private boolean iconsLoaded;

    AccountType(String displayName, int defaultIcon, String iconPath, String lostIconPath)
    {
        this.displayName = displayName;
        this.defaultIcon = defaultIcon;
        this.iconPath = iconPath;
        this.lostIconPath = lostIconPath;
    }

    @Override
    public String toString()
    {
        return displayName;
    }

    public void loadIcons(Client client, ChatIconManager iconManager) {
        if (iconsLoaded)
            return;

        if (iconPath.isEmpty()) {
            icon = defaultIcon;
            return;
        }

        final int iconOffset = client.getModIcons().length;

        try {
            var iconStream = getClass().getResourceAsStream("/" + iconPath);

            if (iconStream == null)
                throw new IllegalStateException("Missing icon: /" + iconPath);

            iconImg = ImageIO.read(iconStream);
            int iconRawId = iconManager.registerChatIcon(iconImg);
            icon = iconRawId + iconOffset - 2;

            var lostIconStream = getClass().getResourceAsStream("/" + lostIconPath);

            if (lostIconStream == null)
                throw new IllegalStateException("Missing lost icon: /" + lostIconPath);

            lostIconImg = ImageIO.read(lostIconStream);
            int lostIconRawId = iconManager.registerChatIcon(lostIconImg);
            lostIcon = lostIconRawId + iconOffset - 2;
            iconsLoaded = true;
        } catch(IOException e) {
            throw new RuntimeException("Failed loading icons for " + this, e);
        }
    }
}