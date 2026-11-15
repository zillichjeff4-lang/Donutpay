package com.donutpay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DonutPayClient implements ClientModInitializer {
    private static final Pattern PAY_CMD =
            Pattern.compile("^/?pay\\s+(\\S+)\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern AMOUNT =
            Pattern.compile("^\\$?([\\d,]*\\.?\\d+)([kmbt]?)$", Pattern.CASE_INSENSITIVE);

    private static final long DURATION_MS = 3000;
    private static final long SLIDE_MS = 250;
    private static final int PANEL_BG = 0xE6101018;
    private static final int PANEL_BORDER = 0xFF3A1C26;
    private static final int AMOUNT_COLOR = 0xFFFF5577;
    private static final int NAME_COLOR = 0xFFE6E6E6;
    private static final int PANEL_H = 32;

    private static String amountText = null;
    private static String targetName = null;
    private static long popupStart = 0;

    @Override
    public void onInitializeClient() {
        ClientSendMessageEvents.COMMAND.register(command -> {
            Matcher m = PAY_CMD.matcher(command.trim());
            if (!m.find()) return;
            Matcher a = AMOUNT.matcher(m.group(2));
            if (!a.matches()) return;
            targetName = m.group(1);
            amountText = "-$" + a.group(1) + a.group(2).toUpperCase();
            popupStart = System.currentTimeMillis();
        });

        HudRenderCallback.EVENT.register(DonutPayClient::render);
    }

    private static void render(DrawContext ctx, RenderTickCounter tick) {
        if (amountText == null) return;
        long age = System.currentTimeMillis() - popupStart;
        if (age > DURATION_MS) { amountText = null; return; }

        MinecraftClient mc = MinecraftClient.getInstance();

        Text amount = Text.literal(amountText).formatted(Formatting.BOLD);
        Text to = Text.literal("to " + targetName);

        int textW = Math.max(mc.textRenderer.getWidth(amount), mc.textRenderer.getWidth(to));
        int w = 6 + 20 + 6 + textW + 10;
        int x = (ctx.getScaledWindowWidth() - w) / 2;

        float p;
        if (age < SLIDE_MS) p = age / (float) SLIDE_MS;
        else if (age > DURATION_MS - SLIDE_MS) p = (DURATION_MS - age) / (float) SLIDE_MS;
        else p = 1f;
        p = 1f - (1f - p) * (1f - p);
        int y = (int) (-PANEL_H + (PANEL_H + 8) * p);

        fillRounded(ctx, x - 1, y - 1, x + w + 1, y + PANEL_H + 1, PANEL_BORDER);
        fillRounded(ctx, x, y, x + w, y + PANEL_H, PANEL_BG);

        int hx = x + 6, hy = y + 6;
        PlayerListEntry entry = mc.getNetworkHandler() == null ? null
                : mc.getNetworkHandler().getPlayerListEntry(targetName);
        if (entry != null) {
            PlayerSkinDrawer.draw(ctx, entry.getSkinTextures(), hx, hy, 20);
        } else {
            ctx.fill(hx, hy, hx + 20, hy + 20, 0xFF553344);
        }

        int tx = hx + 20 + 6;
        ctx.drawTextWithShadow(mc.textRenderer, amount, tx, y + 7, AMOUNT_COLOR);
        ctx.drawTextWithShadow(mc.textRenderer, to, tx, y + 18, NAME_COLOR);
    }

    private static void fillRounded(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1 + 1, y1, x2 - 1, y2, color);
        ctx.fill(x1, y1 + 1, x2, y2 - 1, color);
    }
}
