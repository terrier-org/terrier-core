// Generated by Snowball 2.0.0 - https://snowballstem.org/

package org.terrier.tartarus.snowball.ext;

import org.terrier.tartarus.snowball.Among;

/**
 * This class implements the stemming algorithm defined by a snowball script.
 *
 * <p>Generated by Snowball 2.0.0 - https://snowballstem.org/
 */
@SuppressWarnings("unused")
public class DanishStemmer extends org.terrier.tartarus.snowball.SnowballStemmer {

  private static final long serialVersionUID = 1L;
  private static final java.lang.invoke.MethodHandles.Lookup methodObject =
      java.lang.invoke.MethodHandles.lookup();

  private static final Among a_0[] = {
    new Among("hed", -1, 1),
    new Among("ethed", 0, 1),
    new Among("ered", -1, 1),
    new Among("e", -1, 1),
    new Among("erede", 3, 1),
    new Among("ende", 3, 1),
    new Among("erende", 5, 1),
    new Among("ene", 3, 1),
    new Among("erne", 3, 1),
    new Among("ere", 3, 1),
    new Among("en", -1, 1),
    new Among("heden", 10, 1),
    new Among("eren", 10, 1),
    new Among("er", -1, 1),
    new Among("heder", 13, 1),
    new Among("erer", 13, 1),
    new Among("s", -1, 2),
    new Among("heds", 16, 1),
    new Among("es", 16, 1),
    new Among("endes", 18, 1),
    new Among("erendes", 19, 1),
    new Among("enes", 18, 1),
    new Among("ernes", 18, 1),
    new Among("eres", 18, 1),
    new Among("ens", 16, 1),
    new Among("hedens", 24, 1),
    new Among("erens", 24, 1),
    new Among("ers", 16, 1),
    new Among("ets", 16, 1),
    new Among("erets", 28, 1),
    new Among("et", -1, 1),
    new Among("eret", 30, 1)
  };

  private static final Among a_1[] = {
    new Among("gd", -1, -1),
    new Among("dt", -1, -1),
    new Among("gt", -1, -1),
    new Among("kt", -1, -1)
  };

  private static final Among a_2[] = {
    new Among("ig", -1, 1),
    new Among("lig", 0, 1),
    new Among("elig", 1, 1),
    new Among("els", -1, 1),
    new Among("l\u00F8st", -1, 2)
  };

  private static final char g_c[] = {119, 223, 119, 1};

  private static final char g_v[] = {17, 65, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 0, 128};

  private static final char g_s_ending[] = {
    239, 254, 42, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16
  };

  private int I_x;
  private int I_p1;
  private java.lang.StringBuilder S_ch = new java.lang.StringBuilder();

  private boolean r_mark_regions() {
    I_p1 = limit;
    int v_1 = cursor;
    {
      int c = cursor + 3;
      if (0 > c || c > limit) {
        return false;
      }
      cursor = c;
    }
    I_x = cursor;
    cursor = v_1;
    golab0:
    while (true) {
      int v_2 = cursor;
      lab1:
      {
        if (!(in_grouping(g_v, 97, 248))) {
          break lab1;
        }
        cursor = v_2;
        break golab0;
      }
      cursor = v_2;
      if (cursor >= limit) {
        return false;
      }
      cursor++;
    }
    golab2:
    while (true) {
      lab3:
      {
        if (!(out_grouping(g_v, 97, 248))) {
          break lab3;
        }
        break golab2;
      }
      if (cursor >= limit) {
        return false;
      }
      cursor++;
    }
    I_p1 = cursor;
    lab4:
    {
      if (!(I_p1 < I_x)) {
        break lab4;
      }
      I_p1 = I_x;
    }
    return true;
  }

  private boolean r_main_suffix() {
    int among_var;
    if (cursor < I_p1) {
      return false;
    }
    int v_2 = limit_backward;
    limit_backward = I_p1;
    ket = cursor;
    among_var = find_among_b(a_0);
    if (among_var == 0) {
      limit_backward = v_2;
      return false;
    }
    bra = cursor;
    limit_backward = v_2;
    switch (among_var) {
      case 1:
        slice_del();
        break;
      case 2:
        if (!(in_grouping_b(g_s_ending, 97, 229))) {
          return false;
        }
        slice_del();
        break;
    }
    return true;
  }

  private boolean r_consonant_pair() {
    int v_1 = limit - cursor;
    if (cursor < I_p1) {
      return false;
    }
    int v_3 = limit_backward;
    limit_backward = I_p1;
    ket = cursor;
    if (find_among_b(a_1) == 0) {
      limit_backward = v_3;
      return false;
    }
    bra = cursor;
    limit_backward = v_3;
    cursor = limit - v_1;
    if (cursor <= limit_backward) {
      return false;
    }
    cursor--;
    bra = cursor;
    slice_del();
    return true;
  }

  private boolean r_other_suffix() {
    int among_var;
    int v_1 = limit - cursor;
    lab0:
    {
      ket = cursor;
      if (!(eq_s_b("st"))) {
        break lab0;
      }
      bra = cursor;
      if (!(eq_s_b("ig"))) {
        break lab0;
      }
      slice_del();
    }
    cursor = limit - v_1;
    if (cursor < I_p1) {
      return false;
    }
    int v_3 = limit_backward;
    limit_backward = I_p1;
    ket = cursor;
    among_var = find_among_b(a_2);
    if (among_var == 0) {
      limit_backward = v_3;
      return false;
    }
    bra = cursor;
    limit_backward = v_3;
    switch (among_var) {
      case 1:
        slice_del();
        int v_4 = limit - cursor;
        r_consonant_pair();
        cursor = limit - v_4;
        break;
      case 2:
        slice_from("l\u00F8s");
        break;
    }
    return true;
  }

  private boolean r_undouble() {
    if (cursor < I_p1) {
      return false;
    }
    int v_2 = limit_backward;
    limit_backward = I_p1;
    ket = cursor;
    if (!(in_grouping_b(g_c, 98, 122))) {
      limit_backward = v_2;
      return false;
    }
    bra = cursor;
    slice_to(S_ch);
    limit_backward = v_2;
    if (!(eq_s_b(S_ch))) {
      return false;
    }
    slice_del();
    return true;
  }

  @Override
  public boolean stem() {
    int v_1 = cursor;
    r_mark_regions();
    cursor = v_1;
    limit_backward = cursor;
    cursor = limit;
    int v_2 = limit - cursor;
    r_main_suffix();
    cursor = limit - v_2;
    int v_3 = limit - cursor;
    r_consonant_pair();
    cursor = limit - v_3;
    int v_4 = limit - cursor;
    r_other_suffix();
    cursor = limit - v_4;
    int v_5 = limit - cursor;
    r_undouble();
    cursor = limit - v_5;
    cursor = limit_backward;
    return true;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof DanishStemmer;
  }

  @Override
  public int hashCode() {
    return DanishStemmer.class.getName().hashCode();
  }
}
