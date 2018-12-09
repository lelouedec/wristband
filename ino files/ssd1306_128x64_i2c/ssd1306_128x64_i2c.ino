
#include <Arduino.h>
#include <SPI.h>
#include <string.h>

#if not defined (_VARIANT_ARDUINO_DUE_X_) && not defined (_VARIANT_ARDUINO_ZERO_)
  #include <SoftwareSerial.h>
#endif

#include "Adafruit_BLE.h"
#include "Adafruit_BluefruitLE_SPI.h"
#include "Adafruit_BluefruitLE_UART.h"

#include "BluefruitConfig.h"

#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <CN_SSD1306_Wire.h>

#include "codetab.c"
#include "Bitmaps.h"
#include "max30102.h"


#define OLED_RESET 4
Adafruit_SSD1306 display(OLED_RESET);
CN_SSD1306_Wire lucky(OLED_RESET);

#define NUMFLAKES 10
#define XPOS 0
#define YPOS 1
#define DELTAY 2
//CN_SSD1306_Wire lucky(OLED_RESET);


/*=========================================================================
    APPLICATION SETTINGS

    FACTORYRESET_ENABLE       Perform a factory reset when running this sketch
   
                              Enabling this will put your Bluefruit LE module
                              in a 'known good' state and clear any config
                              data set in previous sketches or projects, so
                              running this at least once is a good idea.
   
                              When deploying your project, however, you will
                              want to disable factory reset by setting this
                              value to 0.  If you are making changes to your
                              Bluefruit LE device via AT commands, and those
                              changes aren't persisting across resets, this
                              is the reason why.  Factory reset will erase
                              the non-volatile memory where config data is
                              stored, setting it back to factory default
                              values.
       
                              Some sketches that require you to bond to a
                              central device (HID mouse, keyboard, etc.)
                              won't work at all with this feature enabled
                              since the factory reset will clear all of the
                              bonding data stored on the chip, meaning the
                              central device won't be able to reconnect.
    MINIMUM_FIRMWARE_VERSION  Minimum firmware version to have some new features
    MODE_LED_BEHAVIOUR        LED activity, valid options are
                              "DISABLE" or "MODE" or "BLEUART" or
                              "HWUART"  or "SPI"  or "MANUAL"
    -----------------------------------------------------------------------*/
    #define FACTORYRESET_ENABLE         1
    #define MINIMUM_FIRMWARE_VERSION    "0.6.6"
    #define MODE_LED_BEHAVIOUR          "MODE"
/*=========================================================================*/

// Create the bluefruit object, either software serial...uncomment these lines
/*
SoftwareSerial bluefruitSS = SoftwareSerial(BLUEFRUIT_SWUART_TXD_PIN, BLUEFRUIT_SWUART_RXD_PIN);

Adafruit_BluefruitLE_UART ble(bluefruitSS, BLUEFRUIT_UART_MODE_PIN,
                      BLUEFRUIT_UART_CTS_PIN, BLUEFRUIT_UART_RTS_PIN);
*/

/* ...or hardware serial, which does not need the RTS/CTS pins. Uncomment this line */
// Adafruit_BluefruitLE_UART ble(BLUEFRUIT_HWSERIAL_NAME, BLUEFRUIT_UART_MODE_PIN);

/* ...hardware SPI, using SCK/MOSI/MISO hardware SPI pins and then user selected CS/IRQ/RST */
Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_CS, BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);

/* ...software SPI, using SCK/MOSI/MISO user-defined SPI pins and then user selected CS/IRQ/RST */
//Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_SCK, BLUEFRUIT_SPI_MISO,
//                             BLUEFRUIT_SPI_MOSI, BLUEFRUIT_SPI_CS,
//                             BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);




#define VBATPIN A9
#define BUFFER_SIZE (100) 
const byte oxiInt = 10; // pin connected to MAX30102 INT



/*#if (SSD1306_LCDHEIGHT != 64)
#error("Height incorrect, please fix Adafruit_SSD1306.h!");
#endif*/

void setup()
{
  uint32_t aun_red_buffer[BUFFER_SIZE]; 
  uint32_t aun_ir_buffer[BUFFER_SIZE];
  uint8_t uch_dummy,k;
  maxim_max30102_reset(); //resets the MAX30102
  delay(1000);
  maxim_max30102_read_reg(REG_INTR_STATUS_1,&uch_dummy);  //Reads/clears the interrupt status register
  maxim_max30102_init(); //initialize the MAX30102
  int i;
  for(i=0;i<BUFFER_SIZE;i++)
  {
    while(digitalRead(oxiInt)==1);  //wait until the interrupt pin asserts
      maxim_max30102_read_fifo((aun_red_buffer+i), (aun_ir_buffer+i)); //read from MAX30102 FIFO
    Serial.print(i, DEC);
    Serial.print(aun_red_buffer[i], DEC);
//    Serial.print(F("\t"));
//    Serial.print(aun_ir_buffer[i], DEC);    
//    Serial.println("");
    //display.clearDisplay();
  }
  display.begin(SSD1306_SWITCHCAPVCC, 0x3C);
  display.display();
  delay(2000);
  if ( !ble.begin(VERBOSE_MODE) )
  {
   writeToScreen("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?");
     delay(2000);
       display.clearDisplay();
  }
  if ( FACTORYRESET_ENABLE )
  {
   if ( ! ble.factoryReset() ){
      writeToScreen("Couldn't factory reset");
    }
  }

  /* Disable command echo from Bluefruit */
  ble.echo(false);
  ble.verbose(false);  // debug info is a little annoying after this point!
    
  display.clearDisplay();
  // miniature bitmap display
  display.drawBitmap(30, 16,  logo16_glcd_bmp, 16, 16, 1);
  display.display();
  delay(1);
  /* Wait for connection */
  while (! ble.isConnected()) {
    writeToScreen("Hello");
    delay(1);
  }
  if ( ble.isVersionAtLeast(MINIMUM_FIRMWARE_VERSION) )
  {
    // Change Mode LED Activity
    ble.sendCommandCheckOK("AT+HWModeLED=" MODE_LED_BEHAVIOUR);
  }

  // Set module to DATA mode
  ble.setMode(BLUEFRUIT_MODE_DATA);
  //display.clearDisplay();

}
String val;
String val2;
void loop() {

  
 
  while ( ble.available()  )
  {   
    int c = ble.read();
    val= val+(char)c;

  }
  
  if(val[val.length()-1]=='0'){
    Serial.print("coucou");
    val2=val;
    val2.remove(val2.length()-1);
    val="";
  }
     
        
  
  if(val=="batterie"){
    DisplayBattery();
    val="";
  }

  if(ble.isConnected()){
     DisplayNotif(val2);
     delay(2000);
  }else{
    val2="";
    val="";
    writeToScreen("Sup' ? ");
  }

}





void DisplayNotif(String notif){
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(WHITE);
  display.setCursor(1,5);

  //scrolling for big text;
  String temp = notif;
  if(notif.length()>60){
    for(int i=0;i<(notif.length()/20);i++){
       display.print(temp);
       display.display();
       delay(1000);
       display.clearDisplay();
       temp.remove(0,20);
       display.setCursor(1,5);
    }
  }else{
    display.print(notif);
    display.display();
    delay(1000);
  }
  
 
}


float measuredvbat;
void DisplayBattery(){
  // DISPLAY BATTERY CHARGE
    display.clearDisplay();
  measuredvbat = analogRead(VBATPIN);// BAttery voltage
  measuredvbat *= 2;    // we divided by 2, so multiply back
  measuredvbat *= 3.3;  // Multiply by 3.3V, our reference voltage
  measuredvbat /= 1024; // convert to voltage
  display.setTextSize(2);
  display.setTextColor(WHITE);
  display.setCursor(0,10);
  /*display.print(" ");
  display.display();*/
  display.print(measuredvbat);
  display.println("V");
  display.display();
  delay(4000);
}




void writeToScreen(String val){
  display.setTextSize(1);
  display.setTextColor(WHITE);
  display.setCursor(10,5);
  display.clearDisplay();
  display.println(val);
  display.display();
}
