import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.util.ArrayList;
import java.awt.geom.RoundRectangle2D;
import com.kitfox.svg.app.beans.SVGIcon;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.awt.image.BufferedImage;

public class MusicPlayerGUI extends JFrame {
    // color config
    public static final Color TEXT_COLOR = new Color(67, 40, 24);
    public static final Color TOOLBAR_COLOR = new Color(255, 137, 167);
    public static final Color MENU_BG_COLOR = new Color(255, 137, 167);
    public static final Color TITLE_TEXT_COLOR = new Color(67, 40, 24);  // Brown

    // allow us tto use file explorer to load songs and folders
    private JFileChooser fileChooser;

    private JButton playPauseButton;
    private SVGIcon backgroundIcon;
    private SVGIcon titleBarIcon; 
    private SVGIcon songBarIcon;
    private SVGIcon musicBarIcon;
    private SVGIcon thumbIcon;
    private SVGIcon verticalBarIcon;
    private SVGIcon songListBGIcon;
    private JLabel albumArtLabel;
    private JLabel songTitleLabel;
    private ArrayList<File> playlist;
    private DefaultListModel<String> playlistModel;
    private JList<String> songList;
    private SVGIcon horizontalBarIcon;
    private MusicPlayer musicPlayer;
    private int currentTrackIndex = 0;
    private JSlider progressSlider;
    private JLabel currentTimeLabel;
    private JLabel totalTimeLabel;
    private Timer progressTimer;
    private boolean shuffleEnabled = false;
    private boolean repeatEnabled = false;




    public MusicPlayerGUI() {

        // calls JFrame constructor to configure out gui and set the title header to ("Music Player")
        super("Sakura Player");
        
        System.setProperty("com.kitfox.svg.antialias", "true");
        System.setProperty("com.kitfox.svg.renderMode", "QUALITY");
        System.setProperty("com.kitfox.svg.textAntiAlias", "true");

        setUndecorated(true);

        // ✅ Custom Application Icon
        try {
            setIconImage(ImageIO.read(getClass().getResourceAsStream("/icon-512.png")));

        } catch (Exception e) {
            // ignore
        }


        // sets width and height of the GUI
        setSize(673, 533);

        // round the window corners
        setBackground(new Color(0, 0, 0, 0)); // Make background transparent

        // end process when app is closed 
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //launch the app at the center of the screen 
        setLocationRelativeTo(null);

        // prevent the app from being resized 
        setResizable(false);

        // Load SVG background
        try {
            backgroundIcon = new SVGIcon();
            backgroundIcon.setSvgURI(getClass().getResource("/Background3.svg").toURI());

            titleBarIcon = new SVGIcon();
            titleBarIcon.setSvgURI(getClass().getResource("/Title Bar3.svg").toURI());

            songBarIcon = new SVGIcon();
            songBarIcon.setSvgURI(getClass().getResource("/Rectangle 2.svg").toURI());

            musicBarIcon = new SVGIcon();
            musicBarIcon.setSvgURI(getClass().getResource("/Music Bar.svg").toURI());

            thumbIcon = new SVGIcon();
            thumbIcon.setSvgURI(getClass().getResource("/Current Place.svg").toURI());

            verticalBarIcon = new SVGIcon();
            verticalBarIcon.setSvgURI(getClass().getResource("/Line 4.svg").toURI());

            songListBGIcon = new SVGIcon();
            songListBGIcon.setSvgURI(getClass().getResource("/Song List BG.svg").toURI());

            horizontalBarIcon = new SVGIcon();
            horizontalBarIcon.setSvgURI(getClass().getResource("/Rectangle 5.svg").toURI());

        } catch (Exception e) {
            
        }



        // Create custom content pane that paints the SVG background
        JPanel contentPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Shape roundedRect = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.setClip(roundedRect);
                
                if (backgroundIcon != null) {
                    double scaleX = (double) getWidth() / backgroundIcon.getIconWidth();
                    double scaleY = (double) getHeight() / backgroundIcon.getIconHeight();
                    g2d.scale(scaleX, scaleY);
                    backgroundIcon.paintIcon(this, g2d, 0, 0);
                }
                    g2d.dispose();
            }
        
        };

        // set layout to null which allows us to control the (x, y) coordinates of our components 
        // and also set the height and width
        contentPane.setLayout(null);
        setContentPane(contentPane);
        contentPane.setBorder(null);

        // add Title Bar Custom Component
        JPanel titleBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
                // Only round the TOP corners (left and right)
                Shape topRoundedRect = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight() + 20, 20, 20);
                g2d.setClip(topRoundedRect);
        
                if (titleBarIcon != null) {
                    double scaleX = (double) getWidth() / titleBarIcon.getIconWidth();
                    double scaleY = (double) getHeight() / titleBarIcon.getIconHeight();
                    g2d.scale(scaleX, scaleY);
                    titleBarIcon.paintIcon(this, g2d, 0, 0);
                }
                g2d.dispose();

            }
            
        };
        titleBar.setBounds(0, 0, 673, 30);
        titleBar.setLayout(null);
        contentPane.add(titleBar);

        JPanel horizontalBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
                // Only round the TOP corners (left and right)
                Shape topRoundedRect = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight() + 20, 20, 20);
                g2d.setClip(topRoundedRect);
        
                if (horizontalBarIcon != null) {
                    double scaleX = (double) getWidth() / horizontalBarIcon.getIconWidth();
                    double scaleY = (double) getHeight() / horizontalBarIcon.getIconHeight();
                    g2d.scale(scaleX, scaleY);
                    horizontalBarIcon.paintIcon(this, g2d, 0, 0);
                }
                g2d.dispose();
            }
        };
        horizontalBar.setBounds(-10, 25, 800, 25);
        horizontalBar.setLayout(null);
        horizontalBar.setOpaque(false);
        contentPane.add(horizontalBar);

        // add title label to title bar
        JLabel titleLabel = new JLabel("Sakura Player");
        titleLabel.setBounds(155, 10, 200, 20);
        titleLabel.setForeground(TITLE_TEXT_COLOR);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleBar.add(titleLabel);

        // add custom close and minimize buttons to title bar
        titleBar.add(createSvgButton("res/Exit2.svg", 10, 8, e -> System.exit(0)));
        titleBar.add(createSvgButton("res/Minimize2.svg", 30, 8, e -> setState(Frame.ICONIFIED)));

        // Make window draggable via title bar
        Point mousePoint = new Point();
        titleBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mousePoint.setLocation(e.getX(), e.getY());
            }
        });
        titleBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(e.getXOnScreen() - mousePoint.x, e.getYOnScreen() - mousePoint.y);
            }
        });

        // add song bar custom component
        JPanel songBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
                // Only round the TOP corners (left and right)
                Shape topRoundedRect = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight() + 20, 20, 20);
                g2d.setClip(topRoundedRect);
        
                if (songBarIcon != null) {
                    double scaleX = (double) getWidth() / songBarIcon.getIconWidth();
                    double scaleY = (double) getHeight() / songBarIcon.getIconHeight();
                    g2d.scale(scaleX, scaleY);
                    songBarIcon.paintIcon(this, g2d, 0, 0);
                }
                g2d.dispose();
            }
        };
        songBar.setBounds(93, 62, 212, 55);
        songBar.setLayout(null);
        songBar.setOpaque(false);
        // Don't block mouse events for components underneath
        songBar.setEnabled(false);
        // Song Title Display
       songTitleLabel = new JLabel("No Song Playing");
       songTitleLabel.setBounds(105, 70, 190, 30);
       songTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
       songTitleLabel.setForeground(new Color(255, 224, 232));
       songTitleLabel.setFont(new Font("PoetsenOne", Font.BOLD, 18));
       add(songTitleLabel);

        contentPane.add(songBar);

        JPanel musicBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
                // Only round the TOP corners (left and right)
                Shape topRoundedRect = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight() + 20, 20, 20);
                g2d.setClip(topRoundedRect);
        
                if (musicBarIcon != null) {
                    double scaleX = (double) getWidth() / musicBarIcon.getIconWidth();
                    double scaleY = (double) getHeight() / musicBarIcon.getIconHeight();
                    g2d.scale(scaleX, scaleY);
                    musicBarIcon.paintIcon(this, g2d, 0, 0);
                }
                g2d.dispose();
            }
        };
        musicBar.setBounds(34, 401, 332, 98);
        musicBar.setLayout(null);
        musicBar.setOpaque(false);
        contentPane.add(musicBar);

         // Playback Control Buttons
        musicBar.add(createSvgButton("res/Back2.svg", 80, 40, e -> playPrevious()));

        // Single Play/Pause toggle button
        playPauseButton = createSvgButton("res/Play2.svg", 142, 40, null);
        musicBar.add(playPauseButton);

        playPauseButton.addActionListener(e -> {
            try {
                if (musicPlayer.isPlaying()) {
                    musicPlayer.pause();
                    // Show PLAY icon when paused
                    SVGIcon playIcon = new SVGIcon();
                    playIcon.setSvgURI(getClass().getResource("/Play2.svg").toURI());

                    playPauseButton.setIcon(playIcon);
                } else {
                    musicPlayer.resume();
                    // Show PAUSE icon when playing
                    SVGIcon pauseIcon = new SVGIcon();
                    pauseIcon.setSvgURI(getClass().getResource("/Pause2.svg").toURI());

                    playPauseButton.setIcon(pauseIcon);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });


        musicBar.add(createSvgButton("res/Next2.svg", 190, 40, e -> playNext()));

        // Shuffle button
        JButton shuffleButton = createSvgButton("res/Shuffle.svg", 44, 46, null);
        shuffleButton.addActionListener(evt -> {
        shuffleEnabled = !shuffleEnabled;
            if (shuffleEnabled) {
                shuffleButton.setBorder(BorderFactory.createLineBorder(new Color(255, 230, 237, 200), 2));
            } else {
                shuffleButton.setBorder(null);
            }
            shuffleButton.repaint();
        });
        musicBar.add(shuffleButton);

        // Repeat button
        JButton repeatButton = createSvgButton("res/Repeat.svg", 253, 46, null);
        repeatButton.addActionListener(evt -> {
            repeatEnabled = !repeatEnabled;
            if (repeatEnabled) {
                repeatButton.setBorder(BorderFactory.createLineBorder(new Color(255, 230, 237, 200), 2));
            } else {
                repeatButton.setBorder(null);
            }
            repeatButton.repaint();
        });
        musicBar.add(repeatButton);

        // Progress Slider (seek bar)
        progressSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        progressSlider.setBounds(40, 10, 250, 20);
        progressSlider.setOpaque(false);
        progressSlider.setForeground(new Color(178, 95, 116));
        progressSlider.setBackground(new Color(255, 200, 210));
        progressSlider.setValue(0);
        progressSlider.setPaintTicks(false);
        progressSlider.setPaintLabels(false);
        
        // Custom slider UI to match the pink theme
        progressSlider.setUI(new BasicSliderUI(progressSlider) {
            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int trackY = trackRect.y + (trackRect.height / 2) - 2;
                int trackHeight = 4;
                
                // Background track
                g2d.setColor(new Color(255, 200, 210, 100));
                g2d.fillRoundRect(trackRect.x, trackY, trackRect.width, trackHeight, 2, 2);
                
                // Filled track (up to thumb position)
                if (thumbRect != null) {
                    g2d.setColor(new Color(178, 95, 116));
                    g2d.fillRoundRect(trackRect.x, trackY, thumbRect.x - trackRect.x, trackHeight, 2, 2);
                }
            }
            
            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 137, 167));
                g2d.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
                g2d.setColor(Color.WHITE);
                g2d.fillOval(thumbRect.x + 2, thumbRect.y + 2, thumbRect.width - 4, thumbRect.height - 4);
            }
            
            @Override
            protected Dimension getThumbSize() {
                return new Dimension(12, 12);
            }
        });
        
        // When user drags the slider, seek to that position
        progressSlider.addMouseListener(new MouseAdapter() {
            private boolean wasPlaying = false;
            
            @Override
            public void mousePressed(MouseEvent e) {
                // Pause the timer while dragging so it doesn't fight the user
                wasPlaying = musicPlayer.isPlaying();
                progressTimer.stop();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                double duration = musicPlayer.getDuration();
                if (duration > 0) {
                    double seekTime = (progressSlider.getValue() / 100.0) * duration;
                    musicPlayer.seek(seekTime);
                }
                // Restart the timer
                progressTimer.start();
            }
        });
        
        musicBar.add(progressSlider);
        
        // Current time label
        currentTimeLabel = new JLabel("0:00");
        currentTimeLabel.setBounds(40, 30, 40, 15);
        currentTimeLabel.setForeground(new Color(67, 40, 24));
        currentTimeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        musicBar.add(currentTimeLabel);
        
        // Total time label
        totalTimeLabel = new JLabel("0:00");
        totalTimeLabel.setBounds(250, 30, 40, 15);
        totalTimeLabel.setForeground(new Color(67, 40, 24));
        totalTimeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        totalTimeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        musicBar.add(totalTimeLabel);
        
        // Timer to update slider position while playing
        progressTimer = new Timer(500, e -> updateProgress());
        progressTimer.start();
        


        // Force music bar to paint first, slider paints after
        contentPane.setComponentZOrder(musicBar, 1);

        JPanel verticalBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
                if (verticalBarIcon != null) {
                    double scaleX = (double) getWidth() / verticalBarIcon.getIconWidth();
                    double scaleY = (double) getHeight() / verticalBarIcon.getIconHeight();
                    g2d.scale(scaleX, scaleY);
                    verticalBarIcon.paintIcon(this, g2d, 0, 0);
                }
                g2d.dispose();
            }
        };
        verticalBar.setBounds(398, 0, 3, 700);
        verticalBar.setLayout(null);
        verticalBar.setOpaque(false);
        contentPane.add(verticalBar);
        contentPane.setComponentZOrder(verticalBar, 1);
        contentPane.setComponentZOrder(titleBar, 2);

        // Song List BG Panel

        JPanel songListBG = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
                // Only round the TOP corners (left and right)
                Shape topRoundedRect = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight() + 20, 30, 30);
                g2d.setClip(topRoundedRect);
        
                if (songListBGIcon != null) {
                    double scaleX = (double) getWidth() / songListBGIcon.getIconWidth();
                    double scaleY = (double) getHeight() / songListBGIcon.getIconHeight();
                    g2d.scale(scaleX, scaleY);
                    songListBGIcon.paintIcon(this, g2d, 0, 0);
                }
                g2d.dispose();
            }
        };
        songListBG.setBounds(417, 66, 234, 450);
        songListBG.setLayout(null);
        songListBG.setOpaque(false);
        contentPane.add(songListBG);

        // Album Art Panel (center area) - plain panel, no background SVG
        JPanel albumPanel = new JPanel(null);
        albumPanel.setBounds(33, 121, 300, 300);
        albumPanel.setOpaque(false);
        contentPane.add(albumPanel);

        // Album art image label - centered inside the album panel
        albumArtLabel = new JLabel();
        albumArtLabel.setBounds(46, 20, 240, 240);
        albumArtLabel.setHorizontalAlignment(SwingConstants.CENTER);
        albumArtLabel.setVerticalAlignment(SwingConstants.CENTER);
        albumPanel.add(albumArtLabel);

        musicPlayer = new MusicPlayer();
        musicPlayer.setOnSongEnd(() -> {
            // When a song ends naturally, play the next one
            // Use SwingUtilities to run on the AWT thread after the FX callback finishes
            SwingUtilities.invokeLater(() -> playNext());
        });
        addGuiComponents();

        // ✅ Initialize Song List
        songList = new JList<>(playlistModel);
        songList.setBackground(new Color(0,0,0,0));
        songList.setOpaque(false);
        songList.setForeground(new Color(67, 40, 24));
        songList.setFont(new Font("Arial", Font.PLAIN, 12));
        songList.setSelectionBackground(new Color(255, 137, 167, 100));
        songList.setSelectionForeground(Color.WHITE);
        songList.setFixedCellHeight(20);
        songList.setBorder(null);

        // Double click to play song
        songList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = songList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        currentTrackIndex = index;
                        playSelectedTrack();
                    }
                }
            }
        });

        // Wrap JList inside JScrollPane
        JScrollPane scrollPane = new JScrollPane(songList);
        scrollPane.setBounds(10, 10, 225, 480);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBackground(new Color(0,0,0,0));

        // Hide horizontal scrollbar (you don't need it)
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Customize vertical scrollbar appearance
        JScrollBar listScrollBar = scrollPane.getVerticalScrollBar();
        listScrollBar.setPreferredSize(new Dimension(8, 0));
        listScrollBar.setBackground(new Color(255, 200, 210, 50));
        listScrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(255, 137, 167);
                this.trackColor = new Color(255, 220, 230, 30);
            }
    
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
    
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
    
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });

        // CORRECT PAINT ORDER (from bottom to top)
        contentPane.setComponentZOrder(verticalBar, 1);

        songListBG.add(scrollPane);

        // ✅ Add Load Song / Load Folder Menu at original position
        JButton loadButton = new JButton("Load Music");
        loadButton.setBounds(0, 25, 90, 25);
        loadButton.setBackground(MENU_BG_COLOR);
        loadButton.setForeground(TEXT_COLOR);
        loadButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        
        JPopupMenu popup = new JPopupMenu();
        
        JMenuItem loadSong = new JMenuItem("Load Single Song");
        loadSong.addActionListener(e -> {
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                addToPlaylist(file);
            }
        });
        
        JMenuItem loadFolder = new JMenuItem("Load Entire Folder");
        loadFolder.addActionListener(e -> {
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File folder = fileChooser.getSelectedFile();
                loadFolderRecursive(folder);
            }
        });
        
        popup.add(loadSong);
        popup.add(loadFolder);
        
        loadButton.addActionListener(e -> popup.show(loadButton, 0, loadButton.getHeight()));
        
        contentPane.add(loadButton);
        contentPane.setComponentZOrder(loadButton, 0); 
    }

    private void addToPlaylist(File file) {
        String name = file.getName().toLowerCase();
        if (file.isFile() && (name.endsWith(".mp3") || name.endsWith(".m4a") || name.endsWith(".wav") || name.endsWith(".flac") || name.endsWith(".aac"))) {
            playlist.add(file);
            // Remove the extension for display
            String displayName = file.getName();
            int dotIndex = displayName.lastIndexOf('.');
            if (dotIndex > 0) {
                displayName = displayName.substring(0, dotIndex);
            }
            playlistModel.addElement(displayName);
        }
    }

    private void loadFolderRecursive(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        loadFolderRecursive(file);
                    } else {
                        addToPlaylist(file);
                    }
                }
            }
        }
    }

    private JButton createSvgButton(String svgPath, int x, int y, ActionListener action) {
        SVGIcon icon = new SVGIcon();
        try {
            // Strip "res/" prefix if present since resources are at JAR root
            String resourcePath = svgPath.startsWith("res/") ? svgPath.substring(4) : svgPath;
            icon.setSvgURI(getClass().getResource("/" + resourcePath).toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JButton button = new JButton(icon);
        button.setBounds(x, y, icon.getIconWidth(), icon.getIconHeight());
        button.setBorder(null);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setRolloverEnabled(false);
        if (action != null) {
            button.addActionListener(action);
        }
        return button;
    }




    private void addGuiComponents() {
        playlist = new ArrayList<>();
        playlistModel = new DefaultListModel<>();
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }

    private void playPrevious() {
        if (currentTrackIndex > 0) {
            currentTrackIndex--;
            playSelectedTrack();
        }
    }

    private void playNext() {
        if (playlist.isEmpty()) return;
        
        if (repeatEnabled) {
            // Loop back to the first song
            if (currentTrackIndex < playlist.size() - 1) {
                currentTrackIndex++;
            } else {
                currentTrackIndex = 0;  // Go back to start
            }
        } else if (shuffleEnabled) {
            // Pick a random song
            if (playlist.size() > 1) {
                int randomIndex;
                do {
                    randomIndex = (int)(Math.random() * playlist.size());
                } while (randomIndex == currentTrackIndex);
                currentTrackIndex = randomIndex;
            }
        } else {
            if (currentTrackIndex < playlist.size() - 1) {
                currentTrackIndex++;
            }
        }
        playSelectedTrack();
    }

    private void playSelectedTrack() {
        try {
            if (currentTrackIndex >= 0 && currentTrackIndex < playlist.size()) {
                File selectedFile = playlist.get(currentTrackIndex);
                musicPlayer.playNewSong(selectedFile);
                
                // Update song title - remove extension
                String displayName = selectedFile.getName();
                int dotIndex = displayName.lastIndexOf('.');
                if (dotIndex > 0) {
                    displayName = displayName.substring(0, dotIndex);
                }
                songTitleLabel.setText(displayName);
                
                // Load album art from metadata
                loadAlbumArt(selectedFile);
                
                // Update button to pause state
                SVGIcon pauseIcon = new SVGIcon();
                pauseIcon.setSvgURI(getClass().getResource("/Pause2.svg").toURI());

                playPauseButton.setIcon(pauseIcon);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads album art from the song file's metadata using jaudiotagger
     * and displays it on the albumArtLabel.
     */
    private void loadAlbumArt(File songFile) {
        try {
            // Read the audio file metadata using jaudiotagger
            AudioFile audioFile = AudioFileIO.read(songFile);
            Tag tag = audioFile.getTag();
            
            if (tag != null) {
                // Try to get artwork - for MP3 this is straightforward
                // For M4A, we need to try a different approach
                org.jaudiotagger.tag.images.Artwork artwork = null;
                
                // Try getting first artwork
                try {
                    artwork = tag.getFirstArtwork();
                } catch (Exception e) {
                    // ignore
                }
                
                if (artwork != null) {
                    byte[] imageData = artwork.getBinaryData();
                    
                    if (imageData != null && imageData.length > 0) {
                        // Convert bytes to BufferedImage
                        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
                        BufferedImage originalImage = ImageIO.read(bais);
                        bais.close();
                        
                        if (originalImage != null) {
                            // Scale the image to fit the label (240x240) while maintaining aspect ratio
                            Image scaledImage = originalImage.getScaledInstance(240, 240, Image.SCALE_SMOOTH);
                            ImageIcon albumIcon = new ImageIcon(scaledImage);
                            albumArtLabel.setIcon(albumIcon);
                            return;
                        }
                    }
                }
                
                // For M4A files, try alternative: get all artwork fields
                try {
                    java.util.List<org.jaudiotagger.tag.images.Artwork> artworkList = tag.getArtworkList();
                    if (artworkList != null && !artworkList.isEmpty()) {
                        for (int i = 0; i < artworkList.size(); i++) {
                            org.jaudiotagger.tag.images.Artwork art = artworkList.get(i);
                            byte[] data = art.getBinaryData();
                            if (data != null && data.length > 0) {
                                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                                BufferedImage img = ImageIO.read(bais);
                                bais.close();
                                if (img != null) {
                                    Image scaledImage = img.getScaledInstance(240, 240, Image.SCALE_SMOOTH);
                                    albumArtLabel.setIcon(new ImageIcon(scaledImage));
                                    return;
                                }
                            }
                        }
                    }
                } catch (Exception e2) {
                    // ignore
                }
            }
            
            // No album art found - clear the label
            albumArtLabel.setIcon(null);
            
        } catch (Exception e) {
            albumArtLabel.setIcon(null);
        }
    }

    private void updateProgress() {
        if (musicPlayer != null && musicPlayer.isPlaying()) {
            double duration = musicPlayer.getDuration();
            double currentTime = musicPlayer.getCurrentTime();
            
            if (duration > 0) {
                int progress = (int) ((currentTime / duration) * 100);
                progressSlider.setValue(progress);
            }
            
            currentTimeLabel.setText(formatTime(currentTime));
            totalTimeLabel.setText(formatTime(duration));
        }
    }
    
    private String formatTime(double seconds) {
        int mins = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%d:%02d", mins, secs);
    }
}
