package br.com.backupautomacao.imagens.helpers;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DateHelper {
  public static String formatDate(long value) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-kkmmssSS", Locale.ENGLISH);
    return simpleDateFormat.format(value);
  }
}
