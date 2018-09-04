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
public class FriendListExporterPlugin extends Plugin
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
