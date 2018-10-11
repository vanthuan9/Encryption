1. Detailed setup:

Installing BouncyCastle:
Navigate to BouncyCastle's "Latest Releases" page and download 'bcprov-jdk15on-160.jar'

-----------------------------------------------

2. Detailed usage:
//The submission includes .java and .jar files.


-----------------------------------------------

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
1-7. Perform the set-up steps

-Send messages from Alice to Mallory to Bob
//In Alice's terminal window, Alice will prompt user for a message to be sent to Bob
8. input: any message

//In Mallory's terminal window, Mallory will prompt user to send, modify, or delete the message
9. input: send
//Bob will now receive and display Alice's message

-Use Mallory to delete a message
10. Repeat of step 8

//In Mallory's terminal window
11. input: modify
12. input: any message to replace Alice's
//Bob will now receive and display Mallory's message

-Use Mallory to modify a message
13. Repeat of step 8

//In Mallory's terminal window
14. input: delete
//The message will not be sent to Bob

-----------------------------------------------

Experiment 2: Encryption Only
1-7. Perform the set-up steps

-Send messages from Alice to Mallory to Bob.
//In Alice's terminal window, Alice will prompt user for a message to be sent to Bob
8. input: any message

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
      String config)                          //One of the four supported modes

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
