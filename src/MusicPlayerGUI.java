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
    private ImageIcon backgroundIcon;
    private ImageIcon titleBarIcon; 
    private ImageIcon songBarIcon;
    private ImageIcon musicBarIcon;
    private ImageIcon verticalBarIcon;
    private ImageIcon songListBGIcon;
    private ImageIcon horizontalBarIcon;
    private JLabel albumArtLabel;
    private JLabel songTitleLabel;
    private ArrayList<File> playlist;
    private DefaultListModel<String> playlistModel;
    private JList<String> songList;
    private MusicPlayer musicPlayer;
    private int currentTrackIndex = 0;
    private JSlider progressSlider;
    private JLabel currentTimeLabel;
    private JLabel totalTimeLabel;
    private Timer progressTimer;
    private boolean shuffleEnabled = false;
    private boolean repeatEnabled = false;

    // Cache for loaded PNG icons — load once, reuse forever
    private final java.util.HashMap<String, ImageIcon> iconCache = new java.util.HashMap<>();

    /** Loads a PNG image from the res/png/ directory (or res/ for icon-512.png).
     *  Results are cached so each icon is only loaded once. */
    private ImageIcon loadPng(String name) {
        // Return cached icon if already loaded
        ImageIcon cached = iconCache.get(name);
        if (cached != null) {
            return cached;
        }
        try {
            ImageIcon icon = null;
            // Try loading from classpath first (works when running from JAR)
            java.net.URL url = getClass().getResource("/png/" + name);
            if (url != null) {
                BufferedImage img = ImageIO.read(url);
                if (img != null) {
                    icon = new ImageIcon(img);
                }
            }
            // Fallback: load from filesystem (works when running from VS Code / IDE)
            if (icon == null) {
                File file = new File("res/png/" + name);
                if (file.exists()) {
                    BufferedImage img = ImageIO.read(file);
                    if (img != null) {
                        icon = new ImageIcon(img);
                    }
                }
            }
            // Cache the result (even if null, to avoid retrying failed loads)
            iconCache.put(name, icon);
            return icon;
        } catch (Exception e) {
            iconCache.put(name, null);
            return null;
        }
    }

    public MusicPlayerGUI() {

        // calls JFrame constructor to configure out gui and set the title header to ("Music Player")
        super("Sakura Player");
        
        setUndecorated(true);

        // ✅ Custom Application Icon
        try {
            java.net.URL iconUrl = getClass().getResource("/icon-512.png");
            if (iconUrl != null) {
                setIconImage(ImageIO.read(iconUrl));
            } else {
                File iconFile = new File("res/icon-512.png");
                if (iconFile.exists()) {
                    setIconImage(ImageIO.read(iconFile));
                }
            }
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

        // Load PNG backgrounds
        backgroundIcon = loadPng("Background3.png");
        titleBarIcon = loadPng("Title Bar3.png");
        songBarIcon = loadPng("Rectangle 2.png");
        musicBarIcon = loadPng("Music Bar.png");
        verticalBarIcon = loadPng("Line 4.png");
        songListBGIcon = loadPng("Song List BG.png");
        horizontalBarIcon = loadPng("Rectangle 5.png");

        // Create custom content pane that paints the PNG background
        JPanel contentPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (backgroundIcon != null) {
                    g2d.drawImage(backgroundIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
                }
                g2d.dispose();
            }
        };

        // set layout to null which allows us to control the (x, y) coordinates of our components 
        // and also set the height and width
        contentPane.setLayout(null);
        contentPane.setOpaque(false);
        setContentPane(contentPane);
        contentPane.setBorder(null);

        // add Title Bar Custom Component
        JPanel titleBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
                if (titleBarIcon != null) {
                    g2d.drawImage(titleBarIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
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
        
                if (horizontalBarIcon != null) {
                    g2d.drawImage(horizontalBarIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
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
        titleBar.add(createClickableIcon("Exit2.png", 10, 8, () -> System.exit(0)));
        titleBar.add(createClickableIcon("Minimize2.png", 30, 8, () -> setState(Frame.ICONIFIED)));

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
        
                if (songBarIcon != null) {
                    g2d.drawImage(songBarIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
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

        // Music bar background image (just for decoration, no children)
        JPanel musicBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
                if (musicBarIcon != null) {
                    g2d.drawImage(musicBarIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
                }
                g2d.dispose();
            }
        };
        musicBar.setBounds(34, 401, 332, 98);
        musicBar.setLayout(null);
        musicBar.setOpaque(false);
        contentPane.add(musicBar);

        // Playback Control Buttons — added directly to contentPane (not musicBar)
        // to avoid parent-child rendering issues with custom-painted panels
        contentPane.add(createClickableIcon("Back2.png", 34 + 80, 401 + 40, () -> playPrevious()));

        // Single Play/Pause toggle button
        playPauseButton = createPngButton("Play2.png", 34 + 142, 401 + 40, null);
        contentPane.add(playPauseButton);

        playPauseButton.addActionListener(e -> {
            try {
                if (musicPlayer.isPlaying()) {
                    musicPlayer.pause();
                    playPauseButton.setIcon(loadPng("Play2.png"));
                } else {
                    musicPlayer.resume();
                    playPauseButton.setIcon(loadPng("Pause2.png"));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        contentPane.add(createPngButton("Next2.png", 34 + 190, 401 + 40, e -> playNext()));

        // Shuffle button
        JButton shuffleButton = createPngButton("Shuffle.png", 34 + 44, 401 + 46, null);
        shuffleButton.addActionListener(evt -> {
        shuffleEnabled = !shuffleEnabled;
            if (shuffleEnabled) {
                shuffleButton.setBorder(BorderFactory.createLineBorder(new Color(255, 230, 237, 200), 2));
            } else {
                shuffleButton.setBorder(null);
            }
            shuffleButton.repaint();
        });
        contentPane.add(shuffleButton);

        // Repeat button
        JButton repeatButton = createPngButton("Repeat.png", 34 + 253, 401 + 46, null);
        repeatButton.addActionListener(evt -> {
            repeatEnabled = !repeatEnabled;
            if (repeatEnabled) {
                repeatButton.setBorder(BorderFactory.createLineBorder(new Color(255, 230, 237, 200), 2));
            } else {
                repeatButton.setBorder(null);
            }
            repeatButton.repaint();
        });
        contentPane.add(repeatButton);

        // Progress Slider (seek bar)
        progressSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        progressSlider.setBounds(34 + 40, 401 + 10, 250, 20);
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
        
        contentPane.add(progressSlider);
        
        // Current time label
        currentTimeLabel = new JLabel("0:00");
        currentTimeLabel.setBounds(34 + 40, 401 + 30, 40, 15);
        currentTimeLabel.setForeground(new Color(67, 40, 24));
        currentTimeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        contentPane.add(currentTimeLabel);
        
        // Total time label
        totalTimeLabel = new JLabel("0:00");
        totalTimeLabel.setBounds(34 + 250, 401 + 30, 40, 15);
        totalTimeLabel.setForeground(new Color(67, 40, 24));
        totalTimeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        totalTimeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        contentPane.add(totalTimeLabel);
        
        // Timer to update slider position while playing
        progressTimer = new Timer(500, e -> updateProgress());
        progressTimer.start();

        JPanel verticalBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
                if (verticalBarIcon != null) {
                    g2d.drawImage(verticalBarIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
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
        
                if (songListBGIcon != null) {
                    g2d.drawImage(songListBGIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
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

    /**
     * Creates a clickable icon using JLabel instead of JButton.
     * JLabel has no hover/press/rollover states, so it won't flicker.
     */
    private JLabel createClickableIcon(String pngName, int x, int y, Runnable action) {
        ImageIcon icon = loadPng(pngName);
        JLabel label = new JLabel(icon);
        if (icon != null) {
            label.setBounds(x, y, icon.getIconWidth(), icon.getIconHeight());
        } else {
            label.setBounds(x, y, 24, 24);
        }
        if (action != null) {
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    action.run();
                }
            });
        }
        return label;
    }

    /**
     * Creates a JButton with a PNG icon.
     */
    private JButton createPngButton(String pngName, int x, int y, ActionListener action) {
        ImageIcon icon = loadPng(pngName);
        JButton button = new JButton(icon);
        if (icon != null) {
            button.setBounds(x, y, icon.getIconWidth(), icon.getIconHeight());
        } else {
            button.setBounds(x, y, 24, 24);
        }
        button.setBorder(null);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setRolloverEnabled(false);
        button.setFocusPainted(false);
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
                playPauseButton.setIcon(loadPng("Pause2.png"));
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
