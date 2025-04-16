package com.seuprojeto.scraper.util;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;

import java.io.*;

public class CookiePersistenceUtil {

    private static final String COOKIE_FILE_PATH = "cookies.ser";

    /**
     * Carrega o CookieStore de um arquivo de serialização (se existir).
     * Caso não exista ou não seja possível ler, retorna um novo BasicCookieStore.
     */
    public static CookieStore loadCookieStore() {
        File file = new File(COOKIE_FILE_PATH);
        if (!file.exists()) {
            return new BasicCookieStore();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof CookieStore) {
                return (CookieStore) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        // Em caso de erro ou arquivo malformado
        return new BasicCookieStore();
    }

    /**
     * Salva o CookieStore em um arquivo de serialização.
     */
    public static void saveCookieStore(CookieStore cookieStore) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(COOKIE_FILE_PATH))) {
            oos.writeObject(cookieStore);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
