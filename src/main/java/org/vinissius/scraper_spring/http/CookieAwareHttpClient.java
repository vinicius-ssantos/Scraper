package org.vinissius.scraper_spring.http;

import com.seuprojeto.scraper.util.CookiePersistenceUtil;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class CookieAwareHttpClient {

    private static CookieStore cookieStore;

    /**
     * Cria (ou obtém) um HttpClient configurado para usar persistência de cookies.
     * Se o cookieStore ainda não foi carregado, ele chama o loadCookieStore().
     */
    public static CloseableHttpClient createClient() {
        if (cookieStore == null) {
            cookieStore = CookiePersistenceUtil.loadCookieStore();
        }

        return HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();
    }

    /**
     * Salva o CookieStore atual em arquivo.
     * Pode ser chamado ao encerrar a aplicação ou a qualquer momento que desejar.
     */
    public static void saveCookies() {
        if (cookieStore != null) {
            CookiePersistenceUtil.saveCookieStore(cookieStore);
        }
    }
}
