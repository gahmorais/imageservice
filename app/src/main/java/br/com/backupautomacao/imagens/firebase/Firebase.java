package br.com.backupautomacao.imagens.firebase;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class Firebase {
  static FirebaseDatabase database;
  static FirebaseStorage storage;

  public static FirebaseDatabase getFirebaseDatabase() {
    if (database == null) {
      database = FirebaseDatabase.getInstance();
      return database;
    }
    return database;
  }

  public static FirebaseStorage getFirebaseStorage(){
    if(storage == null){
      storage = FirebaseStorage.getInstance();
      return storage;
    }
    return storage;
  }
}
