import it.sauronsoftware.jave.Encoder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Music {
    
    public static void main(String[] args) {
        new Music();
    }
    
    public Music() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                }
                
                JFrame frame = new JFrame("Mood Loop V0.1");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                frame.add(new TestPane());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
    
    public class TestPane extends JPanel {
        
        private Clip clip;
        private JTextField audioFile;
        private JButton play;
        private JButton stop;
        private JButton loop, startHere, endHere, loopIt, reset;
        private JTextArea display;
        private int lastFrame;
        private int from, to, ct;
        
        public TestPane() {
            setLayout(new GridBagLayout());
            
            audioFile = new JTextField(20);
            play = new JButton(">");
            stop = new JButton("[ ]");
            loop = new JButton("O");
            startHere = new JButton("->");
            endHere = new JButton("<-");
            loopIt = new JButton("O!");
            reset = new JButton("R");
            display = new JTextArea();
            display.setEditable(false);
            
            JPanel controls = new JPanel();
            JPanel controlsLoop = new JPanel();
            JPanel tField = new JPanel();
            tField.add(display);
            controlsLoop.add(startHere);
            controlsLoop.add(endHere);
            controlsLoop.add(loopIt);
            controlsLoop.add(reset);
            controls.add(play);
            controls.add(stop);
            controls.add(loop);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(audioFile, gbc);
            gbc.gridy++;
            add(controls, gbc);
            gbc.gridy++;
            add(controlsLoop, gbc);
            gbc.gridy++;
            add(tField, gbc);
            startHere.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (clip != null) {
                        from = clip.getFramePosition();
                        display.setText("Playing loop from position " + from);
                    }
                }
            });
            endHere.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (clip != null) {
                        to = clip.getFramePosition();
                        display.setText("Playing loop till: " + to + "\nPress Loop it To start Loop!");
                    }
                }
            });
            loopIt.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (from != 0 && to != 0 && clip != null) {
                        clip.setFramePosition(from);
                        clip.setLoopPoints(from, to);
                        clip.loop(Clip.LOOP_CONTINUOUSLY);
                        display.setText("Mood Playing...");
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "You havent set the loop points or the song isn't playing!", "Error!", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            reset.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (clip != null) {
                        clip.setFramePosition(clip.getFramePosition());
                        clip.loop(0);
                        display.setText("Mood Loop ended! Continuing playback from current position");
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "The song isn't playing!", "Error!", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            
            
            
            
            
            
            loop.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ct++;
                    System.out.println("ct:" + ct);
                    if (ct % 2 == 0) {
                        clip.loop(0);
                        display.setText("Ending infinite Loop");
                    }
                    else {
                        clip.setLoopPoints(0, -1);
                        clip.loop(Clip.LOOP_CONTINUOUSLY);
                        display.setText("Looping continuously");
                    }
                }
            });
            play.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (clip == null) {
                        try {
                            loadClip(new File(audioFile.getText()));
                            clip.start();
                            display.setText("Playing...");
                            clip.addLineListener(new LineListener() {
                                @Override
                                public void update(LineEvent event) {
                                    if (event.getType().equals(Type.START)) {
                                        play.setText("||");
                                    } else if (event.getType().equals(Type.OPEN)) {
                                        System.out.println("Open");
                                    } else if (event.getType().equals(Type.STOP)) {
                                        play.setText(">");
                                    } else if (event.getType().equals(Type.CLOSE)) {
                                        play.setText(">");
                                    }
                                }
                            });
                            play.setText("||");
                        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(TestPane.this, "Failed to load audio clip", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        
                        if (clip.isRunning()) {
                            lastFrame = clip.getFramePosition();
                            System.out.println(lastFrame);
                            clip.stop();
                            display.setText("Paused");
                        } else {
                            if (lastFrame < clip.getFrameLength()) {
                                clip.setFramePosition(lastFrame);
                            } else {
                                clip.setFramePosition(0);
                            }
                            clip.start();
                            display.setText("Playing...");
                        }
                        
                    }
                }
            });
            
            stop.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (clip != null) {
                        ct = 0;
                        lastFrame = 0;
                        clip.stop();
                        clip = null;
                        display.setText("Playback Stopped");
                    }
                }
            });
        }
        
        protected void loadClip(File audioFile) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            this.clip = (Clip) AudioSystem.getLine(info);
            this.clip.open(audioStream);
            
        }
    }
    
}