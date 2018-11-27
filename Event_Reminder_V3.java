import java.util.Calendar;
import java.util.GregorianCalendar;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import static javafx.geometry.Pos.CENTER;

/**
Purpose: An improved version of "Event_Reminder_V2.java"
Programmer: Gabriel Toban Harris
Date: 2018-7-[4, 5]/2018-7-13
*/

//Considered having option to change colours, with a Formattedtextfield in combination with a slider, however such would not get used; thus was not added.
public class Event_Reminder_V3 extends Application implements EventHandler<ActionEvent>
{
        /**
         * Used with back_button to stop sound_then_exit(...).
         */
        private volatile static boolean cancel_endgame;
        /**
         * keep_checking is to end task_update_label_left_time while loop
         */
        private volatile static boolean keep_checking;
        /**
         * Size of both CHECK_BOX_CONTAINER and COMBO_BOX_CONTAINER.
         */
        private final static int CHECK_COMBO_BOX_CONTAINER_SIZE = 5;
        private final static String TOOLTIP_TEXT_CONTAINER[] = new String[]{"Year", "Month", "Day", "Hour", "Minute",
                                                                            "Checked enables corresponding Combo Box, not checked diables corresponding Combo Box.",
                                                                            "Sets target date-time value of the ", " Combo Box."};
        /**
         * Thread for updating LEFT_TIME_LABEL
         */
        private static Thread updating_thread = new Thread();
        /**
         * [0] = year_check_box<br>
         * [1] = month_check_box<br>
         * [2] = day_check_box<br>
         * [3] = hour_check_box<br>
         * [4] = minute_check_box
         */
        private final static CheckBox[] CHECK_BOX_CONTAINER = new CheckBox[CHECK_COMBO_BOX_CONTAINER_SIZE];
        /**
         * [0] = year_combo_box<br>
         * [1] = month_combo_box<br>
         * [2] = day_combo_box<br>
         * [3] = hour_combo_box<br>
         * [4] = minute_combo_box
         */
        @SuppressWarnings("unchecked")//due to how generics work
        private final static ComboBox<Integer>[] COMBO_BOX_CONTAINER = new ComboBox[CHECK_COMBO_BOX_CONTAINER_SIZE];
        private static Button clear_button = new Button("Clear"), submit_button = new Button("Submit"), midnight_activities_button = new Button("Midnight Activities"),
                              back_button = new Button("Back");
        private static Scene access_primary_scene;
        private static Stage access_primary_stage = new Stage();

        public void start(Stage primary_stage)
        {
         HBox horizontal_columns = new HBox(80);
         VBox VBox_label_column = new VBox(32), VBox_check_box_column = new VBox(18), VBox_combo_box_column = new VBox(18);
         VBox_label_column.setAlignment(CENTER);
         VBox_check_box_column.setAlignment(CENTER);
         VBox_combo_box_column.setAlignment(CENTER);

         {
          final Tooltip STYLABLE_CHECK_BOX_TOOLTIP = new Tooltip(TOOLTIP_TEXT_CONTAINER[5]);
          STYLABLE_CHECK_BOX_TOOLTIP.setStyle("-fx-font: 18 arial;");
          for (int i = 0; i < CHECK_COMBO_BOX_CONTAINER_SIZE; i++)
             {
              //1st column labels
              VBox_label_column.getChildren().add(new Label(TOOLTIP_TEXT_CONTAINER[i]));

              //2nd column checkboxes
              CHECK_BOX_CONTAINER[i] = setup_check_box(CHECK_BOX_CONTAINER[i]);
              CHECK_BOX_CONTAINER[i].setTooltip(STYLABLE_CHECK_BOX_TOOLTIP);
              VBox_check_box_column.getChildren().add(CHECK_BOX_CONTAINER[i]);

              //3rd column Combo Boxes
              {
               final Tooltip STYLABLE_COMBO_BOX_TOOLTIP = new Tooltip(TOOLTIP_TEXT_CONTAINER[6] + TOOLTIP_TEXT_CONTAINER[i] + TOOLTIP_TEXT_CONTAINER[7]);
               STYLABLE_COMBO_BOX_TOOLTIP.setStyle("-fx-font: 18 arial;");
               COMBO_BOX_CONTAINER[i] = setup_combo_box(COMBO_BOX_CONTAINER[i], TOOLTIP_TEXT_CONTAINER[i], 6, 134);
               COMBO_BOX_CONTAINER[i].setTooltip(STYLABLE_COMBO_BOX_TOOLTIP);
               VBox_combo_box_column.getChildren().add(COMBO_BOX_CONTAINER[i]);
              }
             }
         }

         //VBoxes
           //1st column
         clear_button.setOnAction(this);
         clear_button.setTooltip(new Tooltip("Resets both checkboxes and Combo Boxes to their defualt values."));
         VBox_label_column.setStyle("-fx-font: 20 arial; -fx-base: #fd4703;");
         VBox_label_column.getChildren().add(clear_button);

           //2nd column
         submit_button.setOnAction(this);
         submit_button.setTooltip(new Tooltip("Goes into Event Reminder mode."));
         VBox_check_box_column.setStyle("-fx-font: 27 arial; -fx-base: #FFFFFF;");
         VBox_check_box_column.getChildren().add(submit_button);
         submit_button.setStyle("-fx-font: 20 arial; -fx-base: #00FF00;");

           //3rd column combo box values
         for (int i = 3; i < CHECK_COMBO_BOX_CONTAINER_SIZE; i++)
             COMBO_BOX_CONTAINER[i].getItems().add(0);
         for (int j = 1; j < CHECK_COMBO_BOX_CONTAINER_SIZE; j++)
             for (int i = 1; i < 13; i++)
                 COMBO_BOX_CONTAINER[j].getItems().add(i);
         for (int j = 2; j < CHECK_COMBO_BOX_CONTAINER_SIZE; j++)
             for (int i = 13; i < 24; i++)
                 COMBO_BOX_CONTAINER[j].getItems().add(i);
         for (int j = 2; j < CHECK_COMBO_BOX_CONTAINER_SIZE; j+=2)
             for (int i = 24; i < 32; i++)
                 COMBO_BOX_CONTAINER[j].getItems().add(i);
         for (int i = 32; i < 60; i++)
             COMBO_BOX_CONTAINER[4].getItems().add(i);
         for (int i = 2017; i < 3000; i++)
             COMBO_BOX_CONTAINER[0].getItems().addAll(i);

         midnight_activities_button.setOnAction(this);
         midnight_activities_button.setTooltip(new Tooltip("Simplified Event Reminder mode for midnight."));
         VBox_combo_box_column.setStyle("-fx-font: 20 arial; -fx-base: #FFFF00;");
         VBox_combo_box_column.getChildren().add(midnight_activities_button);
         midnight_activities_button.setStyle("-fx-base: #FF0000;");

         //everything together
         horizontal_columns.setAlignment(CENTER);
         horizontal_columns.setStyle("-fx-background: #00FFFF;");
         horizontal_columns.getChildren().addAll(VBox_label_column, VBox_check_box_column, VBox_combo_box_column);

         access_primary_stage.setTitle("Event Reminder V3");
         access_primary_stage.setResizable(false);
         access_primary_stage.setScene(access_primary_scene = new Scene(horizontal_columns, 602, 305));
         access_primary_stage.getIcons().add(new Image("/Personal Symbol Without Background gth 2017.png"));
         access_primary_stage.show();

         //other
         back_button.setOnAction(this);
         back_button.setTooltip(new Tooltip("Cancels current reminder and goes back to starting scene."));
        }

        public void handle(ActionEvent e)
        {
         final Object SOURCE = e.getSource();

         for (int i = 0; i < CHECK_COMBO_BOX_CONTAINER_SIZE; i++)
             if (SOURCE == CHECK_BOX_CONTAINER[i])
               {
                check_link_combo(CHECK_BOX_CONTAINER[i], COMBO_BOX_CONTAINER[i]);
                return;//already found target
               }

         if (SOURCE == clear_button)
           {
            for (int i = 0; i < CHECK_COMBO_BOX_CONTAINER_SIZE; i++)
                default_check_combo(CHECK_BOX_CONTAINER[i], COMBO_BOX_CONTAINER[i]);
            return;//already found target
           }

         if (SOURCE == back_button)
           {
            keep_checking = false;
            cancel_endgame = true;
            access_primary_stage.setScene(access_primary_scene);
            return;//already found target
           }

         //must be either submit button xor midnight_activities_button
         else
             {
              Boolean submit_xor_midnight_activities = null;//True for submit and false for midnight_activities. Change to a string with switch statement xor use Method class, in the event that more then 2 possibilities exist for updating LABEL_LEFT_TIME.
              GregorianCalendar GREGORIAN_CALENDAR_TARGET_TIME = null;//target date-time

              if (SOURCE == submit_button)
                {
                 final GregorianCalendar GREGORIAN_CALENDAR_CURRENT_TIME = new GregorianCalendar();
                 if ((CHECK_BOX_CONTAINER[0].isSelected() && COMBO_BOX_CONTAINER[0].getValue() == null) || (CHECK_BOX_CONTAINER[1].isSelected() && COMBO_BOX_CONTAINER[1].getValue() == null) ||
                     (CHECK_BOX_CONTAINER[2].isSelected() && COMBO_BOX_CONTAINER[2].getValue() == null) || (CHECK_BOX_CONTAINER[3].isSelected() && COMBO_BOX_CONTAINER[3].getValue() == null) ||
                     (CHECK_BOX_CONTAINER[4].isSelected() && COMBO_BOX_CONTAINER[4].getValue() == null))
                   {
                    System.err.println("At least one check box is selected with corresponding combo box value not selected.");
                    popup_message("ERROR", "Error", null, "At least one check box is selected with corresponding combo box value not selected.");
                    return;
                   }
                 else if (!CHECK_BOX_CONTAINER[0].isSelected() && !CHECK_BOX_CONTAINER[1].isSelected() && !CHECK_BOX_CONTAINER[2].isSelected() &&
                          !CHECK_BOX_CONTAINER[3].isSelected() && !CHECK_BOX_CONTAINER[4].isSelected())
                     {
                      System.err.println("At least one parameter must be set.");
                      popup_message("ERROR", "Error", null, "At least one parameter must be set.");
                      return;
                     }
                 else if ((CHECK_BOX_CONTAINER[1].isSelected() && CHECK_BOX_CONTAINER[2].isSelected()) &&
                          (((COMBO_BOX_CONTAINER[1].getValue() == 2 || COMBO_BOX_CONTAINER[1].getValue() == 4 || COMBO_BOX_CONTAINER[1].getValue() == 6 || COMBO_BOX_CONTAINER[1].getValue() == 9 || COMBO_BOX_CONTAINER[1].getValue() == 11) && COMBO_BOX_CONTAINER[2].getValue() == 31) ||
                          (COMBO_BOX_CONTAINER[1].getValue() == 2 && COMBO_BOX_CONTAINER[2].getValue() == 30)))
                     {
                      System.err.println("Selected month-day combination doesn't exist.");
                      popup_message("ERROR", "Error", null, "Selected month-day combination doesn't exist.");
                      return;
                     }
                 else if ((CHECK_BOX_CONTAINER[0].isSelected() && CHECK_BOX_CONTAINER[1].isSelected() && CHECK_BOX_CONTAINER[2].isSelected()) &&
                          (((COMBO_BOX_CONTAINER[0].getValue() % 4 != 0) || (COMBO_BOX_CONTAINER[0].getValue() % 100 == 0)) && (COMBO_BOX_CONTAINER[0].getValue() % 400 != 0)) &&
                          (COMBO_BOX_CONTAINER[1].getValue() == 2 && COMBO_BOX_CONTAINER[2].getValue() == 29))
                     {
                      System.err.println("February doesn't have 29 days in selected year");
                      popup_message("ERROR", "Error", null, "February doesn't have 29 days in selected year");
                      return;
                     }
                 else if (calendar_current_compare_target(GREGORIAN_CALENDAR_TARGET_TIME = new GregorianCalendar(CHECK_BOX_CONTAINER[0].isSelected() ? COMBO_BOX_CONTAINER[0].getValue() : GREGORIAN_CALENDAR_CURRENT_TIME.get(GregorianCalendar.YEAR),
                                                                                                                 CHECK_BOX_CONTAINER[1].isSelected() ? COMBO_BOX_CONTAINER[1].getValue() - 1 : GREGORIAN_CALENDAR_CURRENT_TIME.get(GregorianCalendar.MONTH)/* - 1 to convert between calendars*/,
                                                                                                                 CHECK_BOX_CONTAINER[2].isSelected() ? COMBO_BOX_CONTAINER[2].getValue() : GREGORIAN_CALENDAR_CURRENT_TIME.get(GregorianCalendar.DAY_OF_MONTH),
                                                                                                                 CHECK_BOX_CONTAINER[3].isSelected() ? COMBO_BOX_CONTAINER[3].getValue() : GREGORIAN_CALENDAR_CURRENT_TIME.get(GregorianCalendar.HOUR_OF_DAY),
                                                                                                                 CHECK_BOX_CONTAINER[4].isSelected() ? COMBO_BOX_CONTAINER[4].getValue() : GREGORIAN_CALENDAR_CURRENT_TIME.get(GregorianCalendar.MINUTE))))
                     {
                      System.err.println("Selected date and time has already passed.");
                      popup_message("ERROR", "Error", null, "Selected date and time has already passed.");
                      return;
                     }
                 else
                      submit_xor_midnight_activities = new Boolean(true);
                }

              else if (SOURCE == midnight_activities_button)
                  {
                   submit_xor_midnight_activities = new Boolean(false);
                   GREGORIAN_CALENDAR_TARGET_TIME = new GregorianCalendar();

                   //set target time
                   GREGORIAN_CALENDAR_TARGET_TIME.set(GregorianCalendar.HOUR_OF_DAY, 0);
                   GREGORIAN_CALENDAR_TARGET_TIME.set(GregorianCalendar.MINUTE, 0);
                   GREGORIAN_CALENDAR_TARGET_TIME.add(GregorianCalendar.DAY_OF_MONTH, 1);
                  }

              if (submit_xor_midnight_activities != null)
                {
                 final Task<Void> TASK_UPDATE_LABEL_LEFT_TIME;//Task for updating label_left_time
                 {
                  final String TIME_VBOX_STYLE_PIECES[] = new String[2];//for setting style of TIME_VBOX
                  final Label LABEL_LEFT_TIME = new Label(), LABEL_TARGET_TIME = new Label();//labels for TIME_VBOX
                  final AudioClip SOUND_CLIP;

                  //True for submit and false for midnight_activities.
                  if (submit_xor_midnight_activities)
                    {
                     TIME_VBOX_STYLE_PIECES[0] = "20";
                     TIME_VBOX_STYLE_PIECES[1] = "00FF";
                     //strings for label_target_time; magic numbers are date-time related
                     LABEL_TARGET_TIME.setText("Target Time: Years: " +  (CHECK_BOX_CONTAINER[0].isSelected() ? output_subroutine(4, COMBO_BOX_CONTAINER[0].getValue()) : "----") +
                                               ": Months: " + (CHECK_BOX_CONTAINER[1].isSelected() ? output_subroutine(2 , COMBO_BOX_CONTAINER[1].getValue()) : "--") +
                                               ": Days: " + (CHECK_BOX_CONTAINER[2].isSelected() ? output_subroutine(2 , COMBO_BOX_CONTAINER[2].getValue()) : "--") +
                                               ": Hours: " + (CHECK_BOX_CONTAINER[3].isSelected() ? output_subroutine(2 , COMBO_BOX_CONTAINER[3].getValue()) : "--") +
                                               ": Minutes: " + (CHECK_BOX_CONTAINER[4].isSelected() ? output_subroutine(2 , COMBO_BOX_CONTAINER[4].getValue()) : "--"));
                     LABEL_LEFT_TIME.setText(label_left_time_sumbit_button_subroutine());
                     SOUND_CLIP = new AudioClip(getClass().getResource("/Windows Notify.wav").toString());
                    }
                  else
                      {
                       TIME_VBOX_STYLE_PIECES[0] = "32";
                       TIME_VBOX_STYLE_PIECES[1] = "FF00";
                       LABEL_TARGET_TIME.setText("Target Time (Hours:Minutes): 00:00");
                       LABEL_LEFT_TIME.setText(label_left_time_midnight_activities_button_subroutine());
                       SOUND_CLIP = new AudioClip(getClass().getResource("/Windows Ding.wav").toString());
                      }

                  {
                   final VBox TIME_VBOX = new VBox(20, LABEL_LEFT_TIME, LABEL_TARGET_TIME, back_button);
                   TIME_VBOX.setStyle("-fx-font: " + TIME_VBOX_STYLE_PIECES[0] + " courier;-fx-background: #" + TIME_VBOX_STYLE_PIECES[1] + "00;");
                   back_button.setStyle("-fx-font: 20 arial; -fx-base: #FFA500 ;");
                   TIME_VBOX.setAlignment(CENTER);
                   access_primary_stage.setScene(new Scene(TIME_VBOX, 614, 180));
                  }
                  {
                   //for TASK_UPDATE_LABEL_LEFT_TIME "...defined in an enclosing scope must be final or effectively final".
                   final boolean CHOSEN_ONE = submit_xor_midnight_activities;
                   final GregorianCalendar TARGET_DATE_TIME = GREGORIAN_CALENDAR_TARGET_TIME;

                   TASK_UPDATE_LABEL_LEFT_TIME = new Task<Void>()
                   {
                    public Void call() throws InterruptedException
                    {
                     final Function_Bank.temporal_discrepancy_class TIME_KEEPER = new Function_Bank.temporal_discrepancy_class();

                     wait_seconds_is_0();

                     while (keep_checking)
                          {
                           if (TIME_KEEPER.temporal_discrepancy(65000))
                             {
                              System.err.println("Warning: a temporal discrepancy has occured");
                              //popup_message("WARNING", "Warning", null, "Warning: a temporal discrepancy has occured");
                              if (calendar_current_compare_target(TARGET_DATE_TIME))
                                {
                                 endgame("The target time has already passed.", SOUND_CLIP);
                                 continue;
                                }
                              wait_seconds_is_0();
                             }

                           updateMessage((CHOSEN_ONE) ? label_left_time_sumbit_button_subroutine() : label_left_time_midnight_activities_button_subroutine());

                           if (calendar_current_compare_target(TARGET_DATE_TIME))
                              endgame("The target time has been reached.", SOUND_CLIP);
                           else
                                Thread.sleep(60000);
                          }
                     return null;
                    }
                   };
                   TASK_UPDATE_LABEL_LEFT_TIME.messageProperty().addListener((obs, oldMessage, newMessage) -> LABEL_LEFT_TIME.setText(newMessage));
                  }
                 }
                 {
                  try
                     {
                      updating_thread.join();//To allow previous thread to die before altering booleans: keep_checking and cancel_endgame.
                     }
                  catch (InterruptedException ex)
                       {
                        ex.printStackTrace();
                       }

                  updating_thread = new Thread(TASK_UPDATE_LABEL_LEFT_TIME);
                  updating_thread.setDaemon(true);
                  keep_checking = true;
                  cancel_endgame = false;
                  updating_thread.start();
                 }
                }
             }
        }

        /**
         * to check if target date-time has passed
         * @param calendar_target_time time to be compared to current time.
         * @return true when time has past, else false
         */
        private static boolean calendar_current_compare_target(Calendar calendar_target_time)
        {
         return Calendar.getInstance().getTimeInMillis() >= calendar_target_time.getTimeInMillis() ? true : false;
        }

        /**
         * Subroutine to update and make text for label_left_time as used by the sumbit_button variant.
         */
        private String label_left_time_sumbit_button_subroutine()
        {
         final GregorianCalendar GREGORIAN_CALENDAR_CURRENT_TIME = new GregorianCalendar();

         //String pieces can be negative if current > parameter; default values for labels in case of corresponding check box not being selected.
         return "    Left Time: Years: " + (CHECK_BOX_CONTAINER[0].isSelected() ? output_subroutine(4, COMBO_BOX_CONTAINER[0].getValue() - GREGORIAN_CALENDAR_CURRENT_TIME.get(GregorianCalendar.YEAR)) : "----") +
                ": Months: " + (CHECK_BOX_CONTAINER[1].isSelected() ? output_subroutine(2, COMBO_BOX_CONTAINER[1].getValue() - GREGORIAN_CALENDAR_CURRENT_TIME.get(GregorianCalendar.MONTH)) : "--") +
                ": Days: " + (CHECK_BOX_CONTAINER[2].isSelected() ? output_subroutine(2, COMBO_BOX_CONTAINER[2].getValue() - GREGORIAN_CALENDAR_CURRENT_TIME.get(GregorianCalendar.DAY_OF_MONTH)) : "--") +
                ": Hours: " + (CHECK_BOX_CONTAINER[3].isSelected() ? output_subroutine(2, COMBO_BOX_CONTAINER[3].getValue() - GREGORIAN_CALENDAR_CURRENT_TIME.get(GregorianCalendar.HOUR_OF_DAY)) : "--") +
                ": Minutes: " + (CHECK_BOX_CONTAINER[4].isSelected() ? output_subroutine(2, COMBO_BOX_CONTAINER[4].getValue() - GREGORIAN_CALENDAR_CURRENT_TIME.get(GregorianCalendar.MINUTE)) : "--");
        }

        /**
         * Subroutine to update individual pieces making up label_left_time as used by the midnight_activities_button variant.
         */
        private static String label_left_time_midnight_activities_button_subroutine()
        {
         final GregorianCalendar GREGORIAN_CALENDAR_CURRENT_TIME = new GregorianCalendar();
         int current_hour = GREGORIAN_CALENDAR_CURRENT_TIME.get(GregorianCalendar.HOUR_OF_DAY);
         int current_minute = GREGORIAN_CALENDAR_CURRENT_TIME.get(GregorianCalendar.MINUTE);

         //60 minutes = 1 hour, for label_left_time and -1 also for flag.
         if (current_minute == 0)
           {
            current_minute = 60;
            current_hour--;
           }

         return "    Left Time (Hours:Minutes): " + (current_hour == -1 ? "00:00" : output_subroutine(2, 23 - current_hour) + ":" + output_subroutine(2, 60 - current_minute));
        }

        /**
         * Function to aid functions forming label_left_time's text. By adding spacing 0s to have desired number of digits when otherwise would not.
         *
         * @param output_size size of desired output
         * @param input thing being padded
         * @return part of the label's string
         */
        private static String output_subroutine(int output_size, Object input)
        {
         String place_holder = input.toString();
         while (place_holder.length() < output_size)
               place_holder = "0" + place_holder;

         return place_holder;
        }

        /**
         * set up check boxes
         * @param setup reference to CheckBox being initialized
         */
        private CheckBox setup_check_box(CheckBox setup)
        {
         setup = new CheckBox();
         setup.setSelected(true);
         setup.setOnAction(this);
         return setup;
        }

        /**
         * set up combo box
         * @param setup reference to ComboBox being initialized
         * @param prompt text for the ComboBox
         * @param row number of visible rows for ComboBox
         * @param min_width of ComboBox
         */
        private static <G> ComboBox<G> setup_combo_box(ComboBox<G> setup, String prompt, int row, double min_width)
        {
         setup = new ComboBox<G>();
         setup.setPromptText(prompt);
         setup.setVisibleRowCount(row);
         setup.setMinWidth(min_width);
         return setup;
        }

        /**
         * Align seconds with minutes by having system wait until is 'new minute' (0 in seconds place).
         * @throws InterruptedException from Thread.sleep(long millis)
         */
        private static void wait_seconds_is_0() throws InterruptedException
        {
         if (Calendar.getInstance().get(Calendar.SECOND) != 0)
            Thread.sleep((60 - Calendar.getInstance().get(Calendar.SECOND)) * 1000);//sleep until seconds is lined up with minutes
        }

        /**
         * last set of steps before application ends
         * @param message to be outputted to console
         * @param clip to play have sound_then_exit(...) play
         * @throws InterruptedException from Thread.sleep(long millis)
         */
        private void endgame(String message, AudioClip clip) throws InterruptedException
        {
         keep_checking = false;
         System.out.println(message);
         sound_then_exit(clip, 5, 30, 300000);
        }

        /**
         * CheckBox toggles corresponding ComboBox's enabledness.
         * @param check_box to act as toggler
         * @param combo_box object being toggled
         */
        private static <G> void check_link_combo(CheckBox check_box, ComboBox<G> combo_box)
        {
         if (!check_box.isSelected())
           {
            combo_box.setValue(null);
            combo_box.setDisable(true);
           }
         else
              combo_box.setDisable(false);
        }

        /**
         * subroutine for "clear_button"
         * @param check_box being reset
         * @param combo_box being reset
         */
        private static <G> void default_check_combo(CheckBox check_box, ComboBox<G> combo_box)
        {
         combo_box.setValue(null);
         combo_box.setDisable(false);
         check_box.setSelected(true);
        }

        /**
         * pop-up messages
         * @param type of Alert to be displayed
         * @param title of window
         * @param header of message
         * @param content main message displayed
         */
        private static void popup_message(String type, String title, String header, String content)
        {
         Alert popup = new Alert(Alert.AlertType.valueOf(type));
         popup.setTitle(title);
         popup.setHeaderText(header);
         popup.setContentText(content);
         popup.setResizable(false);
         popup.showAndWait();
        }

        /**
         * loop playing sound then close program
         * @param clip AudioClip to play
         * @param times number of times to call the clip
         * @param cycles number of times the clip is played when called
         * @param period amount of time in milliseconds between playing the AudioClip
         * @throws InterruptedException from Thread.sleep(long millis)
         */
        private static void sound_then_exit(AudioClip clip, int times, int cycles, int period) throws InterruptedException
        {
         clip.setCycleCount(cycles);
         for (int i = 0; i < times; i++)
            {
             if (cancel_endgame)
                return;
             clip.play();
             Thread.sleep(period);
            }
         System.exit(0);
        }

        //For both Eclipse and runnable JAR.
        public static void main(String[] args){launch(args);}
}
