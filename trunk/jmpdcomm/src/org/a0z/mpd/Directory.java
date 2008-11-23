package org.a0z.mpd;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

/**
 * Class representing a directory.
 * @author Felipe Gustavo de Almeida
 * @version $Id: Directory.java 2614 2004-11-11 18:46:31Z galmeida $
 */
public final class Directory {
    private Map files;

    private Map directories;

    private Directory parent;

    private String name;

    private MPD mpd;

    /**
     * Creates a new directory.
     * @param mpd MPD controller.
     * @param parent parent directory.
     * @param name directory name.
     */
    private Directory(MPD mpd, Directory parent, String name) {
        this.mpd = mpd;
        this.name = name;
        this.parent = parent;
        this.directories = new HashMap();
        this.files = new HashMap();
    }

    /**
     * Creates a new directory.
     * @param mpd MPD controller.
     * @return last path component.
     */
    public static Directory makeRootDirectory(MPD mpd) {
        return new Directory(mpd, null, "");
    }

    /**
     * Retrieves directory name.
     * @return directory name.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves files from directory.
     * @return files from directory.
     */
    public Collection getFiles() {
        Collection c = new TreeSet(new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Music) o1).getFilename().compareTo(((Music) o2).getFilename());
            }
        });
        Iterator it = files.keySet().iterator();
        while (it.hasNext()) {
            Object o = files.get(it.next());
            c.add(o);
        }
        return c;
    }

    /**
     * Retrieves subdirectories.
     * @return subdirectories.
     */
    public Collection getDirectories() {
        Collection c = new TreeSet(new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Directory) o1).getName().compareTo(((Directory) o2).getName());
            }
        });
        Iterator it = directories.keySet().iterator();
        while (it.hasNext()) {
            Object o = directories.get(it.next());
            c.add(o);
        }
        return c;
    }

    /**
     * Refresh directorie contents (not recursive).
     * @throws MPDServerException if an error occurs while contacting server.
     */
    public void refreshData() throws MPDServerException {
        Collection c = mpd.getDir(this.getFullpath());
        Iterator it = c.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Directory) {
                Directory dir = (Directory) o;
                if (!directories.containsKey(dir.getName())) {
                    directories.put(dir.getName(), dir);
                }
            } else if (o instanceof Music) {
                Music music = (Music) o;
                if (!files.containsKey(music.getFilename())) {
                    files.put(music.getFilename(), music);
                } else {
                    Music old = (Music) files.get(music.getFilename());
                    old.update(music);
                }
            }
        }
    }

    /**
     * Given a path not starting or ending with '/', creates all dirs on this
     * path.
     * @param name path, must not start or end with '/'.
     * @return the last component of the path created.
     */
    public Directory makeDirectory(String name) {
        String firstName;
        String lastName;
        int slashIndex = name.indexOf('/');

        if (slashIndex == 0) {
            throw new InvalidParameterException("name starts with '/'");
        }
        if (slashIndex == -1) {
            firstName = name;
            lastName = null;
        } else {
            firstName = name.substring(0, slashIndex);
            lastName = name.substring(slashIndex + 1);
        }
        Directory dir;
        if (!directories.containsKey(firstName)) {
            dir = new Directory(mpd, this, firstName);
            directories.put(dir.getName(), dir);
        } else {
            dir = (Directory) directories.get(firstName);
        }

        if (lastName != null) {
            return dir.makeDirectory(lastName);
        }
        return dir;
    }

    /**
     * Adds a file, creating path directories (mkdir -p).
     * @param file file to be added
     */
    public void addFile(Music file) {
        Directory dir = this;
        if (this.getFullpath().compareTo(file.getPath()) == 0) {
            file.setParent(this);
            files.put(file.getFilename(), file);
        } else {
            dir = makeDirectory(file.getPath());
            dir.addFile(file);
        }
    }

    /**
     * Check if a given directory existis as a subdir.
     * @param name subdir name.
     * @return true if subdir exists, false if not.
     */
    public boolean containsDir(String name) {
        return directories.containsKey(name);
    }

    /**
     * Retrieves a subdirectory.
     * @param name name of subdirectory to retrieve.
     * @return a subdirectory.
     */
    public Directory getDirectory(String name) {
        return (Directory) directories.get(name);
    }

    /**
     * Retrieves a textual representation of this object.
     * @return textual representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("+" + this.getFullpath() + "\n");
        Iterator it = files.keySet().iterator();
        while (it.hasNext()) {
            sb.append(files.get(it.next()) + "\n");
        }

        it = directories.keySet().iterator();
        while (it.hasNext()) {
            sb.append(directories.get(it.next()));
        }
        sb.append("-" + this.getFullpath() + "\n");
        return sb.toString();
    }

    /**
     * Retrieves parent directory.
     * @return parent directory.
     */
    public Directory getParent() {
        return parent;
    }

    /**
     * Retrieves directory's fullpathname. (does not starts with /)
     * @return fullpathname of this directory.
     */
    public String getFullpath() {
        if (this.getParent() != null && this.getParent().getParent() != null) {
            return this.getParent().getFullpath() + "/" + this.getName();
        } else {
            return this.getName();
        }
    }
}