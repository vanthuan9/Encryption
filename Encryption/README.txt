1. Detailed setup:

This program was coded and tested on a machine running on macOS High Sierra,
no other operating system has been tested.

This program uses BouncyCastle, but the relevant packages should be included
along with the .jar files. In order to modify the code, import the .java files
into a Java Project. Make sure to install BouncyCastle.

Installing BouncyCastle:
-This is a guide on how to install BouncyCastle on the Eclipse IDE
Navigate to BouncyCastle's "Latest Releases" page and download 'bcprov-jdk15on-160.jar'
Go to your Java Package in Eclipse and select 'Build Path' > 'Configure Build Path'
  continue with 'Libraries' > 'Add External JARs' > *select the downloaded .jar file

-----------------------------------------------

2. Detailed usage:
//The submission includes .java and .jar files.

Set-up Steps:  //should be run before every experiment
1. Start a new terminal window, navigate to where .jar files are located
2. input: java -jar Gen.jar APublicKey.pem APrivateKey.pem BPublicKey.pem BPrivateKey.pem
3. input: java -jar Bob.jar APublicKey.pem BPublicKey.pem BPrivateKey.pem 2001 noCrypt
4. Start another terminal window, navigate to .jar files
5. input: java -jar Mallory.jar APublicKey.pem BPublicKey.pem 2000 2001 noCrypt
6. Start another terminal window, navigate to .jar files
7. input: java -jar Alice.jar APublicKey.pem APrivateKey.pem BPublicKey.pem 2000 noCrypt



-----------------------------------------------

Experiment 1: No encryption
1. Start a new terminal window, navigate to where .jar files are located
2. input: java -jar Gen.jar APublicKey.pem APrivateKey.pem BPublicKey.pem BPrivateKey.pem
3. input: java -jar Bob.jar APublicKey.pem BPublicKey.pem BPrivateKey.pem 2001 noCrypt
4. Start another terminal window, navigate to .jar files
5. input: java -jar Mallory.jar APublicKey.pem BPublicKey.pem 2000 2001 noCrypt
6. Start another terminal window, navigate to .jar files
7. input: java -jar Alice.jar APublicKey.pem APrivateKey.pem BPublicKey.pem 2000 noCrypt

-Send messages from Alice to Mallory to Bob
//In Alice's terminal window, Alice will prompt user for a message to be sent to Bob
8. input: *any message

//In Mallory's terminal window, Mallory will prompt user to send, modify, or delete the message
9. input: send
//Bob will now receive and display Alice's message

-Use Mallory to delete a message
10. Repeat of step 8

//In Mallory's terminal window
11. input: delete
//The message will not be sent to Bob

-Use Mallory to modify a message
12. Repeat of step 8

//In Mallory's terminal window
13. input: modify
14. input: *copy and paste Alice's entire transmission except with the counter and message altered
//make sure the the counter is subtracted by 1 since the last message was deleted, or Bob will detect it
//Bob will now receive and display Mallory's message


-----------------------------------------------

Experiment 2: Encryption Only
1. Start a new terminal window, navigate to where .jar files are located
2. input: java -jar Gen.jar APublicKey.pem APrivateKey.pem BPublicKey.pem BPrivateKey.pem
3. input: java -jar Bob.jar APublicKey.pem BPublicKey.pem BPrivateKey.pem 2001 encOnly
4. Start another terminal window, navigate to .jar files
5. input: java -jar Mallory.jar APublicKey.pem BPublicKey.pem 2000 2001 encOnly
6. Start another terminal window, navigate to .jar files
7. input: java -jar Alice.jar APublicKey.pem APrivateKey.pem BPublicKey.pem 2000 encOnly

-Send messages from Alice to Mallory to Bob.
//In Alice's terminal window, Alice will prompt user for a message to be sent to Bob
8. input: *any message

//In Mallory's terminal window, Mallory will prompt user to send, modify, or delete the message
9. input: send

//Since the message is encrypted Mallory will not be able to reliably modify
//it in a way that will still be intelligible to Bob

//Bob receives the message from Alice, decrypts it, and output to terminal

-----------------------------------------------

Experiment 3: Mac Only
1. Start a new terminal window, navigate to where .jar files are located
2. input: java -jar Gen.jar APublicKey.pem APrivateKey.pem BPublicKey.pem BPrivateKey.pem
3. input: java -jar Bob.jar APublicKey.pem BPublicKey.pem BPrivateKey.pem 2001 macOnly
4. Start another terminal window, navigate to .jar files
5. input: java -jar Mallory.jar APublicKey.pem BPublicKey.pem 2000 2001 macOnly
6. Start another terminal window, navigate to .jar files
7. input: java -jar Alice.jar APublicKey.pem APrivateKey.pem BPublicKey.pem 2000 macOnly

//Since MAC is enabled, Alice will attempt to send a MAC key to Bob. As Mallory:
7.5 input: send

-Send messages from Alice to Mallory to Bob.
//In Alice's terminal window, Alice will prompt user for a message to be sent to Bob
8. input: *any message

//In Mallory's terminal window, Mallory will prompt user to send, modify, or delete the message
9. input: send

~repeat step 8-9 as many time as you think is necessary

-Use Mallory to replay an old message.
//In Mallory's terminal window
10. input: replay
//Mallory will display the list of accumulated messages along with an index
11. input: *numerical index of the message to be replayed

//Bob will receive the message, but will also recognize the replay attack

-Use Mallory to delete a message and pass the next message through.
//As Alice, send 2 messages to Bob
12. Repeat of step 8
13. Repeat of step 8

//In Mallory's terminal window
14. input: delete //the first message
15. input: send

//Bob will receive the message, but will also recognize the replay attack





-----------------------------------------------

Experiment 4: Enc-then-Mac
1. Start a new terminal window, navigate to where .jar files are located
2. input: java -jar Gen.jar APublicKey.pem APrivateKey.pem BPublicKey.pem BPrivateKey.pem
3. input: java -jar Bob.jar APublicKey.pem BPublicKey.pem BPrivateKey.pem 2001 mac&Enc
4. Start another terminal window, navigate to .jar files
5. input: java -jar Mallory.jar APublicKey.pem BPublicKey.pem 2000 2001 mac&Enc
6. Start another terminal window, navigate to .jar files
7. input: java -jar Alice.jar APublicKey.pem APrivateKey.pem BPublicKey.pem 2000 mac&Enc

//Since MAC is enabled, Alice will attempt to send a MAC key to Bob. As Mallory:
7.5 input: send

-Send messages from Alice to Mallory to Bob.
//In Alice's terminal window, Alice will prompt user for a message to be sent to Bob
8. input: *any message

//In Mallory's terminal window, Mallory will prompt user to send, modify, or delete the message
9. input: send

//Since the message is encrypted Mallory will not be able to reliably modify
//it in a way that will still be intelligible to Bob

//Bob receives the message from Alice, decrypts it, and output to terminal

-----------------------------------------------

List of valid config:
  noCrypt - No Cryptography mode
  macOnly - MACs only
  encOnly - Symmetric encryption only mode
  mac&Enc - Symmetric encryption then MAC

-----------------------------------------------

Parameters of important classes:
Gen(String alicePubKeyFile,                 //Location of Alice's public key
    String alicePrivateKeyFile,             //Location of Alice's private key
    String bobPubKeyFile,                   //Location of Bob's public key
    String bobPrivateKeyFile)               //Location of Bob's private key

Alice(String alicePubKeyFile,               //Location of Alice's public key
      String alicePrivateKeyFile,           //Location of Alice's private key
      String bobPubKeyFile,                 //Location of Bob's public key
      String malPort,                       //Port for Alice to connect to Mallory
      String config)                        //One of the four supported modes

Bob(String alicePubKeyFile,                 //Location of Alice's public key
    String bobPubKeyFile,                   //Location of Bob's public key
    String bobPrivateKeyFile,               //Location of Bob's private key
    String bobPort,                         //Port for Mallory to connect to Bob
    String config)                          //One of the four supported modes

Mallory(String alicePubKey,                 //Location of Alice's public key
        String bobPubKey,                   //Location of Bob's public key
        String malPort,                     //Port for Alice to connect to Mallory
        String bobPort,                     //Port for Mallory to connect to Bob
        String config)                      //One of the four supported modes
