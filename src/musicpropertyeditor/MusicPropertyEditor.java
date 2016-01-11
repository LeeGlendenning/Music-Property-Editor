package musicpropertyeditor;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.*;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;

/**
 *
 * @author Lee
 */
public class MusicPropertyEditor {

    private static String getArtist(String s) {
        int i = 0;
        try {
            //find last index of artist
            while (s.length() > i + 2 && (!s.substring(i + 1, i + 3).equals(" -") || !s.substring(i + 1, i + 2).equals("-"))) {
                i++;
            }
        } catch (Exception e) {
            System.out.println("Bad file name. Artist");
        }
        return s.substring(0, i + 1);
    }

    private static String getTitle(String s) {
        int i = 0, j = 0;
        try {
            //find first index of title
            while (s.length() > i + 2 && (!s.substring(i + 1, i + 3).equals(" -") || !s.substring(i + 1, i + 2).equals("-"))) {
                i++;
            }
            
            j = i;
            if (s.charAt(i) == ' '){
                j ++;
            }
            while (!s.substring(j + 1, j + 2).equals(".")) {
                j++;
            }
        } catch (Exception e) {
            System.out.println("Bad file name. Title");
        }

        return s.substring(i + 4, j + 1);
    }
    

    public void editMusicFiles(File rootDir) throws IOException, CannotReadException, TagException,
            ReadOnlyFileException, InvalidAudioFrameException, KeyNotFoundException, FieldDataInvalidException,
            CannotWriteException {
        //MusicPropertyEditor properties = new MusicPropertyEditor();
        List<String> files = getFileList(rootDir.toPath());

        File curFile;

        for (int i = 0; i < files.size(); i++) {
            curFile = new File(files.get(i));
            //AudioFileIO.logger.getParent().setLevel(Level.OFF);
            
            System.out.println("Working on:");
            System.out.println("Artist: " + getArtist(curFile.getName()));
            System.out.println("Title: " + getTitle(curFile.getName()));
            System.out.println(getExtension(curFile.getName()));
            System.out.println();
            
            if (getExtension(curFile.getName()).equals(".m4a") || curFile.getName().equals(".mp4") || curFile.getName().equals(".m4p")) {
                AudioFile f = AudioFileIO.read(curFile);
                editMp4(f, getArtist(curFile.getName()), getTitle(curFile.getName()));
                
            } else if (getExtension(curFile.getName()).equals(".mp3")) {
                MP3File f = (MP3File) AudioFileIO.read(curFile);
                editMp3(f, getArtist(curFile.getName()), getTitle(curFile.getName()));
            //} else if (getExtension(curFile.getName()).equals(".ogg")){
                //AudioFile f = AudioFileIO.read(curFile);
            //    AudioFile f = AudioFileIO.readMagic(curFile);
            //    editOgg(f, getArtist(curFile.getName()), getTitle(curFile.getName()));
            }
        }
        
    }

    private static String getExtension(String s) {
        int i = 0;
        while (s.charAt(i) != '.') {
            i++;
        }

        return s.substring(i);
    }

    private static void editMp4(AudioFile f, String artist, String title) throws KeyNotFoundException,
            FieldDataInvalidException, CannotWriteException {
        Mp4Tag mp4tag = (Mp4Tag) f.getTag();
        mp4tag.setField(Mp4FieldKey.ARTIST, artist);
        mp4tag.setField(Mp4FieldKey.TITLE, title);

        f.setTag(mp4tag);
        f.commit();
    }

    private static void editMp3(MP3File f, String artist, String title) throws KeyNotFoundException,
            FieldDataInvalidException, CannotWriteException {
        Tag mp3tag;
         
        mp3tag = new ID3v23Tag();
        mp3tag.setField(FieldKey.ARTIST, artist);
        mp3tag.setField(FieldKey.TITLE, title);

        f.setTag(mp3tag);
        f.commit();
    }
    
    /*private static void editOgg(AudioFile f, String artist, String title) throws KeyNotFoundException,
            FieldDataInvalidException, CannotWriteException {
        
        Tag oggtag = (VorbisCommentTag)f.getTag();
        
        oggtag.setField(FieldKey.ARTIST, artist);
        oggtag.setField(FieldKey.TITLE, title);

        f.setTag(oggtag);
        f.commit();
    }*/

    private static List<String> getFileList(Path path) throws IOException {
        Deque<Path> stack = new ArrayDeque<>();
        final List<String> files = new LinkedList<>();

        stack.push(path);

        while (!stack.isEmpty()) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(stack.pop())) {
                for (Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        stack.push(entry);
                    } else {
                        files.add(entry.toString());
                    }
                }
            }
        }

        return files;
    }
}
