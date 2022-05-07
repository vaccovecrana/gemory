package io.vacco.gemory.util;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.layout.Region;

public class GmCss {

  public static final String
      GmNordDeepDark = "#242931",
      // Polar night
      GmNord0 = "#2E3440", GmNord1 = "#3B4252", GmNord2 = "#434C5E", GmNord3 = "#4C566A",
      // Snow storm
      GmNord4 = "#D8DEE9", GmNord5 = "#E5E9F0", GmNord6 = "#ECEFF4",
      // Frost
      GmNord7 = "#8FBCBB", GmNord8 = "#88C0D0", GmNord9 = "#81A1C1", GmNord10 = "#5E81AC",
      // Aurora
      GmNord11 = "#BF616A", GmNord12 = "#D08770", GmNord13 = "#EBCB8B", GmNord14 = "#A3BE8C", GmNord15 = "#B48EAD";

  public static void applyNord(Parent p) {
    p.getStylesheets().add("/io/vacco/gemory/gm-nord.css");
  }

  public static void pad(Region rg, int topRightBottomLeft) {
    rg.setPadding(new Insets(topRightBottomLeft, topRightBottomLeft, topRightBottomLeft, topRightBottomLeft));
  }

  public static void pad(Region rg, int t, int r, int b, int l) {
    rg.setPadding(new Insets(t, r, b, l));
  }

  public static void pad(Region rg, int t, int b) {
    rg.setPadding(new Insets(t, 0, b, 0));
  }

  public static Region empty(int t, int r, int b, int l) {
    var rg = new Region();
    pad(rg, t, r, b, l);
    return rg;
  }

}
