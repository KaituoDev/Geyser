/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.geyser.session.cache;

import com.github.steveice10.mc.protocol.packet.ingame.serverbound.inventory.ServerboundEditBookPacket;
import lombok.Setter;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.item.Items;
import org.geysermc.geyser.session.GeyserSession;

/**
 * Manages updating the current writable book.
 *
 * Java sends book updates less frequently than Bedrock, and this can cause issues with servers that rate limit
 * book packets. Because of this, we need to ensure packets are only send every second or so at maximum.
 */
public class BookEditCache {
    private final GeyserSession session;
    @Setter
    private ServerboundEditBookPacket packet;
    /**
     * Stores the last time a book update packet was sent to the server.
     */
    private long lastBookUpdate;

    public BookEditCache(GeyserSession session) {
        this.session = session;
    }

    /**
     * Check to see if there is a book edit update to send, and if so, send it.
     */
    public void checkForSend() {
        if (packet == null) {
            // No new packet has to be sent
            return;
        }
        // Prevent kicks due to rate limiting - specifically on Spigot servers
        if ((System.currentTimeMillis() - lastBookUpdate) < 1000) {
            return;
        }
        // Don't send the update if the player is not holding a book, shouldn't happen if we catch all interactions
        GeyserItemStack itemStack = session.getPlayerInventory().getItemInHand();
        if (itemStack == null || itemStack.asItem() != Items.WRITABLE_BOOK) {
            packet = null;
            return;
        }
        session.sendDownstreamGamePacket(packet);
        packet = null;
        lastBookUpdate = System.currentTimeMillis();
    }
}
