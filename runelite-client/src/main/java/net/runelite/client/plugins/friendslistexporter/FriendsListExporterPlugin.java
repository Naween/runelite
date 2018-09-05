/*
 * Copyright (c) 2018, Forsco <https://github.com/forsco>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.runelite.client.plugins.friendslistexporter;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Friend;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import javax.swing.filechooser.FileSystemView;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@PluginDescriptor(
        name = "Friend List Exporter",
        description = "Store friends list to file."
)
public class FriendsListExporterPlugin extends Plugin
{
    public boolean exported;

    @Inject
    private Client client;

    @Override
    protected void startUp()
    {
        exported = false;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameState)
    {
        exported = false;

        if (gameState.getGameState() == GameState.LOGGED_IN)
        {
            try
            {
                export();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick)
    {
        if (!exported)
        {
            try
            {
                export();;
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void export() throws Exception
    {
        if (!exported)
        {
            Path file = Paths.get(FileSystemView.getFileSystemView().getHomeDirectory().getPath());
            System.out.println("hi");
            try
            {
                Files.createFile(Paths.get(file.toString() + "/Friends.txt"));
                Path friendFile = Paths.get(file.toString() + "/Friends.txt");
                PrintWriter writer = new PrintWriter(new FileWriter(friendFile.toFile()));
                for (Friend friend : client.getFriends())
                {
                    if (friend != null)
                    {
                        writer.println(friend.getName());
                    }
                }
                writer.flush();
                writer.close();
                log.debug("Friend Export Complete: " + Paths.get(FileSystemView.getFileSystemView().getHomeDirectory().getPath()));
            }
            catch (FileAlreadyExistsException e)
            {
                PrintWriter writer = new PrintWriter(FileSystemView.getFileSystemView().getHomeDirectory().getPath() + "/Friends.txt");
                writer.print("");
                for (Friend friend : client.getFriends())
                {
                    if (friend != null)
                    {
                        writer.println(friend.getName());
                    }
                }
                writer.flush();
                writer.close();
                log.debug("Friend Export Complete: " + Paths.get(FileSystemView.getFileSystemView().getHomeDirectory().getPath()));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        exported = true;
    }
}
