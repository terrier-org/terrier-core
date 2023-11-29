// Generated by Snowball 2.0.0 - https://snowballstem.org/

package org.terrier.tartarus.snowball.ext;

import org.terrier.tartarus.snowball.Among;

/**
 * This class implements the stemming algorithm defined by a snowball script.
 *
 * <p>Generated by Snowball 2.0.0 - https://snowballstem.org/
 */
@SuppressWarnings("unused")
public class ArabicStemmer extends org.terrier.tartarus.snowball.SnowballStemmer {

  private static final long serialVersionUID = 1L;
  private static final java.lang.invoke.MethodHandles.Lookup methodObject =
      java.lang.invoke.MethodHandles.lookup();

  private static final Among a_0[] = {
    new Among("\u0640", -1, 1),
    new Among("\u064B", -1, 1),
    new Among("\u064C", -1, 1),
    new Among("\u064D", -1, 1),
    new Among("\u064E", -1, 1),
    new Among("\u064F", -1, 1),
    new Among("\u0650", -1, 1),
    new Among("\u0651", -1, 1),
    new Among("\u0652", -1, 1),
    new Among("\u0660", -1, 2),
    new Among("\u0661", -1, 3),
    new Among("\u0662", -1, 4),
    new Among("\u0663", -1, 5),
    new Among("\u0664", -1, 6),
    new Among("\u0665", -1, 7),
    new Among("\u0666", -1, 8),
    new Among("\u0667", -1, 9),
    new Among("\u0668", -1, 10),
    new Among("\u0669", -1, 11),
    new Among("\uFE80", -1, 12),
    new Among("\uFE81", -1, 16),
    new Among("\uFE82", -1, 16),
    new Among("\uFE83", -1, 13),
    new Among("\uFE84", -1, 13),
    new Among("\uFE85", -1, 17),
    new Among("\uFE86", -1, 17),
    new Among("\uFE87", -1, 14),
    new Among("\uFE88", -1, 14),
    new Among("\uFE89", -1, 15),
    new Among("\uFE8A", -1, 15),
    new Among("\uFE8B", -1, 15),
    new Among("\uFE8C", -1, 15),
    new Among("\uFE8D", -1, 18),
    new Among("\uFE8E", -1, 18),
    new Among("\uFE8F", -1, 19),
    new Among("\uFE90", -1, 19),
    new Among("\uFE91", -1, 19),
    new Among("\uFE92", -1, 19),
    new Among("\uFE93", -1, 20),
    new Among("\uFE94", -1, 20),
    new Among("\uFE95", -1, 21),
    new Among("\uFE96", -1, 21),
    new Among("\uFE97", -1, 21),
    new Among("\uFE98", -1, 21),
    new Among("\uFE99", -1, 22),
    new Among("\uFE9A", -1, 22),
    new Among("\uFE9B", -1, 22),
    new Among("\uFE9C", -1, 22),
    new Among("\uFE9D", -1, 23),
    new Among("\uFE9E", -1, 23),
    new Among("\uFE9F", -1, 23),
    new Among("\uFEA0", -1, 23),
    new Among("\uFEA1", -1, 24),
    new Among("\uFEA2", -1, 24),
    new Among("\uFEA3", -1, 24),
    new Among("\uFEA4", -1, 24),
    new Among("\uFEA5", -1, 25),
    new Among("\uFEA6", -1, 25),
    new Among("\uFEA7", -1, 25),
    new Among("\uFEA8", -1, 25),
    new Among("\uFEA9", -1, 26),
    new Among("\uFEAA", -1, 26),
    new Among("\uFEAB", -1, 27),
    new Among("\uFEAC", -1, 27),
    new Among("\uFEAD", -1, 28),
    new Among("\uFEAE", -1, 28),
    new Among("\uFEAF", -1, 29),
    new Among("\uFEB0", -1, 29),
    new Among("\uFEB1", -1, 30),
    new Among("\uFEB2", -1, 30),
    new Among("\uFEB3", -1, 30),
    new Among("\uFEB4", -1, 30),
    new Among("\uFEB5", -1, 31),
    new Among("\uFEB6", -1, 31),
    new Among("\uFEB7", -1, 31),
    new Among("\uFEB8", -1, 31),
    new Among("\uFEB9", -1, 32),
    new Among("\uFEBA", -1, 32),
    new Among("\uFEBB", -1, 32),
    new Among("\uFEBC", -1, 32),
    new Among("\uFEBD", -1, 33),
    new Among("\uFEBE", -1, 33),
    new Among("\uFEBF", -1, 33),
    new Among("\uFEC0", -1, 33),
    new Among("\uFEC1", -1, 34),
    new Among("\uFEC2", -1, 34),
    new Among("\uFEC3", -1, 34),
    new Among("\uFEC4", -1, 34),
    new Among("\uFEC5", -1, 35),
    new Among("\uFEC6", -1, 35),
    new Among("\uFEC7", -1, 35),
    new Among("\uFEC8", -1, 35),
    new Among("\uFEC9", -1, 36),
    new Among("\uFECA", -1, 36),
    new Among("\uFECB", -1, 36),
    new Among("\uFECC", -1, 36),
    new Among("\uFECD", -1, 37),
    new Among("\uFECE", -1, 37),
    new Among("\uFECF", -1, 37),
    new Among("\uFED0", -1, 37),
    new Among("\uFED1", -1, 38),
    new Among("\uFED2", -1, 38),
    new Among("\uFED3", -1, 38),
    new Among("\uFED4", -1, 38),
    new Among("\uFED5", -1, 39),
    new Among("\uFED6", -1, 39),
    new Among("\uFED7", -1, 39),
    new Among("\uFED8", -1, 39),
    new Among("\uFED9", -1, 40),
    new Among("\uFEDA", -1, 40),
    new Among("\uFEDB", -1, 40),
    new Among("\uFEDC", -1, 40),
    new Among("\uFEDD", -1, 41),
    new Among("\uFEDE", -1, 41),
    new Among("\uFEDF", -1, 41),
    new Among("\uFEE0", -1, 41),
    new Among("\uFEE1", -1, 42),
    new Among("\uFEE2", -1, 42),
    new Among("\uFEE3", -1, 42),
    new Among("\uFEE4", -1, 42),
    new Among("\uFEE5", -1, 43),
    new Among("\uFEE6", -1, 43),
    new Among("\uFEE7", -1, 43),
    new Among("\uFEE8", -1, 43),
    new Among("\uFEE9", -1, 44),
    new Among("\uFEEA", -1, 44),
    new Among("\uFEEB", -1, 44),
    new Among("\uFEEC", -1, 44),
    new Among("\uFEED", -1, 45),
    new Among("\uFEEE", -1, 45),
    new Among("\uFEEF", -1, 46),
    new Among("\uFEF0", -1, 46),
    new Among("\uFEF1", -1, 47),
    new Among("\uFEF2", -1, 47),
    new Among("\uFEF3", -1, 47),
    new Among("\uFEF4", -1, 47),
    new Among("\uFEF5", -1, 51),
    new Among("\uFEF6", -1, 51),
    new Among("\uFEF7", -1, 49),
    new Among("\uFEF8", -1, 49),
    new Among("\uFEF9", -1, 50),
    new Among("\uFEFA", -1, 50),
    new Among("\uFEFB", -1, 48),
    new Among("\uFEFC", -1, 48)
  };

  private static final Among a_1[] = {
    new Among("\u0622", -1, 1),
    new Among("\u0623", -1, 1),
    new Among("\u0624", -1, 1),
    new Among("\u0625", -1, 1),
    new Among("\u0626", -1, 1)
  };

  private static final Among a_2[] = {
    new Among("\u0622", -1, 1),
    new Among("\u0623", -1, 1),
    new Among("\u0624", -1, 2),
    new Among("\u0625", -1, 1),
    new Among("\u0626", -1, 3)
  };

  private static final Among a_3[] = {
    new Among("\u0627\u0644", -1, 2),
    new Among("\u0628\u0627\u0644", -1, 1),
    new Among("\u0643\u0627\u0644", -1, 1),
    new Among("\u0644\u0644", -1, 2)
  };

  private static final Among a_4[] = {
    new Among("\u0623\u0622", -1, 2),
    new Among("\u0623\u0623", -1, 1),
    new Among("\u0623\u0624", -1, 1),
    new Among("\u0623\u0625", -1, 4),
    new Among("\u0623\u0627", -1, 3)
  };

  private static final Among a_5[] = {new Among("\u0641", -1, 1), new Among("\u0648", -1, 1)};

  private static final Among a_6[] = {
    new Among("\u0627\u0644", -1, 2),
    new Among("\u0628\u0627\u0644", -1, 1),
    new Among("\u0643\u0627\u0644", -1, 1),
    new Among("\u0644\u0644", -1, 2)
  };

  private static final Among a_7[] = {
    new Among("\u0628", -1, 1), new Among("\u0628\u0628", 0, 2), new Among("\u0643\u0643", -1, 3)
  };

  private static final Among a_8[] = {
    new Among("\u0633\u0623", -1, 4),
    new Among("\u0633\u062A", -1, 2),
    new Among("\u0633\u0646", -1, 3),
    new Among("\u0633\u064A", -1, 1)
  };

  private static final Among a_9[] = {
    new Among("\u062A\u0633\u062A", -1, 1),
    new Among("\u0646\u0633\u062A", -1, 1),
    new Among("\u064A\u0633\u062A", -1, 1)
  };

  private static final Among a_10[] = {
    new Among("\u0643\u0645\u0627", -1, 3),
    new Among("\u0647\u0645\u0627", -1, 3),
    new Among("\u0646\u0627", -1, 2),
    new Among("\u0647\u0627", -1, 2),
    new Among("\u0643", -1, 1),
    new Among("\u0643\u0645", -1, 2),
    new Among("\u0647\u0645", -1, 2),
    new Among("\u0647\u0646", -1, 2),
    new Among("\u0647", -1, 1),
    new Among("\u064A", -1, 1)
  };

  private static final Among a_11[] = {new Among("\u0646", -1, 1)};

  private static final Among a_12[] = {
    new Among("\u0627", -1, 1), new Among("\u0648", -1, 1), new Among("\u064A", -1, 1)
  };

  private static final Among a_13[] = {new Among("\u0627\u062A", -1, 1)};

  private static final Among a_14[] = {new Among("\u062A", -1, 1)};

  private static final Among a_15[] = {new Among("\u0629", -1, 1)};

  private static final Among a_16[] = {new Among("\u064A", -1, 1)};

  private static final Among a_17[] = {
    new Among("\u0643\u0645\u0627", -1, 3),
    new Among("\u0647\u0645\u0627", -1, 3),
    new Among("\u0646\u0627", -1, 2),
    new Among("\u0647\u0627", -1, 2),
    new Among("\u0643", -1, 1),
    new Among("\u0643\u0645", -1, 2),
    new Among("\u0647\u0645", -1, 2),
    new Among("\u0643\u0646", -1, 2),
    new Among("\u0647\u0646", -1, 2),
    new Among("\u0647", -1, 1),
    new Among("\u0643\u0645\u0648", -1, 3),
    new Among("\u0646\u064A", -1, 2)
  };

  private static final Among a_18[] = {
    new Among("\u0627", -1, 1),
    new Among("\u062A\u0627", 0, 2),
    new Among("\u062A\u0645\u0627", 0, 4),
    new Among("\u0646\u0627", 0, 2),
    new Among("\u062A", -1, 1),
    new Among("\u0646", -1, 1),
    new Among("\u0627\u0646", 5, 3),
    new Among("\u062A\u0646", 5, 2),
    new Among("\u0648\u0646", 5, 3),
    new Among("\u064A\u0646", 5, 3),
    new Among("\u064A", -1, 1)
  };

  private static final Among a_19[] = {
    new Among("\u0648\u0627", -1, 1), new Among("\u062A\u0645", -1, 1)
  };

  private static final Among a_20[] = {
    new Among("\u0648", -1, 1), new Among("\u062A\u0645\u0648", 0, 2)
  };

  private static final Among a_21[] = {new Among("\u0649", -1, 1)};

  private boolean B_is_defined;
  private boolean B_is_verb;
  private boolean B_is_noun;

  private boolean r_Normalize_pre() {
    int among_var;
    int v_1 = cursor;
    lab0:
    {
      while (true) {
        int v_2 = cursor;
        lab1:
        {
          lab2:
          {
            int v_3 = cursor;
            lab3:
            {
              bra = cursor;
              among_var = find_among(a_0);
              if (among_var == 0) {
                break lab3;
              }
              ket = cursor;
              switch (among_var) {
                case 1:
                  slice_del();
                  break;
                case 2:
                  slice_from("0");
                  break;
                case 3:
                  slice_from("1");
                  break;
                case 4:
                  slice_from("2");
                  break;
                case 5:
                  slice_from("3");
                  break;
                case 6:
                  slice_from("4");
                  break;
                case 7:
                  slice_from("5");
                  break;
                case 8:
                  slice_from("6");
                  break;
                case 9:
                  slice_from("7");
                  break;
                case 10:
                  slice_from("8");
                  break;
                case 11:
                  slice_from("9");
                  break;
                case 12:
                  slice_from("\u0621");
                  break;
                case 13:
                  slice_from("\u0623");
                  break;
                case 14:
                  slice_from("\u0625");
                  break;
                case 15:
                  slice_from("\u0626");
                  break;
                case 16:
                  slice_from("\u0622");
                  break;
                case 17:
                  slice_from("\u0624");
                  break;
                case 18:
                  slice_from("\u0627");
                  break;
                case 19:
                  slice_from("\u0628");
                  break;
                case 20:
                  slice_from("\u0629");
                  break;
                case 21:
                  slice_from("\u062A");
                  break;
                case 22:
                  slice_from("\u062B");
                  break;
                case 23:
                  slice_from("\u062C");
                  break;
                case 24:
                  slice_from("\u062D");
                  break;
                case 25:
                  slice_from("\u062E");
                  break;
                case 26:
                  slice_from("\u062F");
                  break;
                case 27:
                  slice_from("\u0630");
                  break;
                case 28:
                  slice_from("\u0631");
                  break;
                case 29:
                  slice_from("\u0632");
                  break;
                case 30:
                  slice_from("\u0633");
                  break;
                case 31:
                  slice_from("\u0634");
                  break;
                case 32:
                  slice_from("\u0635");
                  break;
                case 33:
                  slice_from("\u0636");
                  break;
                case 34:
                  slice_from("\u0637");
                  break;
                case 35:
                  slice_from("\u0638");
                  break;
                case 36:
                  slice_from("\u0639");
                  break;
                case 37:
                  slice_from("\u063A");
                  break;
                case 38:
                  slice_from("\u0641");
                  break;
                case 39:
                  slice_from("\u0642");
                  break;
                case 40:
                  slice_from("\u0643");
                  break;
                case 41:
                  slice_from("\u0644");
                  break;
                case 42:
                  slice_from("\u0645");
                  break;
                case 43:
                  slice_from("\u0646");
                  break;
                case 44:
                  slice_from("\u0647");
                  break;
                case 45:
                  slice_from("\u0648");
                  break;
                case 46:
                  slice_from("\u0649");
                  break;
                case 47:
                  slice_from("\u064A");
                  break;
                case 48:
                  slice_from("\u0644\u0627");
                  break;
                case 49:
                  slice_from("\u0644\u0623");
                  break;
                case 50:
                  slice_from("\u0644\u0625");
                  break;
                case 51:
                  slice_from("\u0644\u0622");
                  break;
              }
              break lab2;
            }
            cursor = v_3;
            if (cursor >= limit) {
              break lab1;
            }
            cursor++;
          }
          continue;
        }
        cursor = v_2;
        break;
      }
    }
    cursor = v_1;
    return true;
  }

  private boolean r_Normalize_post() {
    int among_var;
    int v_1 = cursor;
    lab0:
    {
      limit_backward = cursor;
      cursor = limit;
      ket = cursor;
      if (find_among_b(a_1) == 0) {
        break lab0;
      }
      bra = cursor;
      slice_from("\u0621");
      cursor = limit_backward;
    }
    cursor = v_1;
    int v_2 = cursor;
    lab1:
    {
      while (true) {
        int v_3 = cursor;
        lab2:
        {
          lab3:
          {
            int v_4 = cursor;
            lab4:
            {
              bra = cursor;
              among_var = find_among(a_2);
              if (among_var == 0) {
                break lab4;
              }
              ket = cursor;
              switch (among_var) {
                case 1:
                  slice_from("\u0627");
                  break;
                case 2:
                  slice_from("\u0648");
                  break;
                case 3:
                  slice_from("\u064A");
                  break;
              }
              break lab3;
            }
            cursor = v_4;
            if (cursor >= limit) {
              break lab2;
            }
            cursor++;
          }
          continue;
        }
        cursor = v_3;
        break;
      }
    }
    cursor = v_2;
    return true;
  }

  private boolean r_Checks1() {
    int among_var;
    bra = cursor;
    among_var = find_among(a_3);
    if (among_var == 0) {
      return false;
    }
    ket = cursor;
    switch (among_var) {
      case 1:
        if (!(limit > 4)) {
          return false;
        }
        B_is_noun = true;
        B_is_verb = false;
        B_is_defined = true;
        break;
      case 2:
        if (!(limit > 3)) {
          return false;
        }
        B_is_noun = true;
        B_is_verb = false;
        B_is_defined = true;
        break;
    }
    return true;
  }

  private boolean r_Prefix_Step1() {
    int among_var;
    bra = cursor;
    among_var = find_among(a_4);
    if (among_var == 0) {
      return false;
    }
    ket = cursor;
    switch (among_var) {
      case 1:
        if (!(limit > 3)) {
          return false;
        }
        slice_from("\u0623");
        break;
      case 2:
        if (!(limit > 3)) {
          return false;
        }
        slice_from("\u0622");
        break;
      case 3:
        if (!(limit > 3)) {
          return false;
        }
        slice_from("\u0627");
        break;
      case 4:
        if (!(limit > 3)) {
          return false;
        }
        slice_from("\u0625");
        break;
    }
    return true;
  }

  private boolean r_Prefix_Step2() {
    {
      int v_1 = cursor;
      lab0:
      {
        if (!(eq_s("\u0641\u0627"))) {
          break lab0;
        }
        return false;
      }
      cursor = v_1;
    }
    {
      int v_2 = cursor;
      lab1:
      {
        if (!(eq_s("\u0648\u0627"))) {
          break lab1;
        }
        return false;
      }
      cursor = v_2;
    }
    bra = cursor;
    if (find_among(a_5) == 0) {
      return false;
    }
    ket = cursor;
    if (!(limit > 3)) {
      return false;
    }
    slice_del();
    return true;
  }

  private boolean r_Prefix_Step3a_Noun() {
    int among_var;
    bra = cursor;
    among_var = find_among(a_6);
    if (among_var == 0) {
      return false;
    }
    ket = cursor;
    switch (among_var) {
      case 1:
        if (!(limit > 5)) {
          return false;
        }
        slice_del();
        break;
      case 2:
        if (!(limit > 4)) {
          return false;
        }
        slice_del();
        break;
    }
    return true;
  }

  private boolean r_Prefix_Step3b_Noun() {
    int among_var;
    {
      int v_1 = cursor;
      lab0:
      {
        if (!(eq_s("\u0628\u0627"))) {
          break lab0;
        }
        return false;
      }
      cursor = v_1;
    }
    bra = cursor;
    among_var = find_among(a_7);
    if (among_var == 0) {
      return false;
    }
    ket = cursor;
    switch (among_var) {
      case 1:
        if (!(limit > 3)) {
          return false;
        }
        slice_del();
        break;
      case 2:
        if (!(limit > 3)) {
          return false;
        }
        slice_from("\u0628");
        break;
      case 3:
        if (!(limit > 3)) {
          return false;
        }
        slice_from("\u0643");
        break;
    }
    return true;
  }

  private boolean r_Prefix_Step3_Verb() {
    int among_var;
    bra = cursor;
    among_var = find_among(a_8);
    if (among_var == 0) {
      return false;
    }
    ket = cursor;
    switch (among_var) {
      case 1:
        if (!(limit > 4)) {
          return false;
        }
        slice_from("\u064A");
        break;
      case 2:
        if (!(limit > 4)) {
          return false;
        }
        slice_from("\u062A");
        break;
      case 3:
        if (!(limit > 4)) {
          return false;
        }
        slice_from("\u0646");
        break;
      case 4:
        if (!(limit > 4)) {
          return false;
        }
        slice_from("\u0623");
        break;
    }
    return true;
  }

  private boolean r_Prefix_Step4_Verb() {
    bra = cursor;
    if (find_among(a_9) == 0) {
      return false;
    }
    ket = cursor;
    if (!(limit > 4)) {
      return false;
    }
    B_is_verb = true;
    B_is_noun = false;
    slice_from("\u0627\u0633\u062A");
    return true;
  }

  private boolean r_Suffix_Noun_Step1a() {
    int among_var;
    ket = cursor;
    among_var = find_among_b(a_10);
    if (among_var == 0) {
      return false;
    }
    bra = cursor;
    switch (among_var) {
      case 1:
        if (!(limit >= 4)) {
          return false;
        }
        slice_del();
        break;
      case 2:
        if (!(limit >= 5)) {
          return false;
        }
        slice_del();
        break;
      case 3:
        if (!(limit >= 6)) {
          return false;
        }
        slice_del();
        break;
    }
    return true;
  }

  private boolean r_Suffix_Noun_Step1b() {
    ket = cursor;
    if (find_among_b(a_11) == 0) {
      return false;
    }
    bra = cursor;
    if (!(limit > 5)) {
      return false;
    }
    slice_del();
    return true;
  }

  private boolean r_Suffix_Noun_Step2a() {
    ket = cursor;
    if (find_among_b(a_12) == 0) {
      return false;
    }
    bra = cursor;
    if (!(limit > 4)) {
      return false;
    }
    slice_del();
    return true;
  }

  private boolean r_Suffix_Noun_Step2b() {
    ket = cursor;
    if (find_among_b(a_13) == 0) {
      return false;
    }
    bra = cursor;
    if (!(limit >= 5)) {
      return false;
    }
    slice_del();
    return true;
  }

  private boolean r_Suffix_Noun_Step2c1() {
    ket = cursor;
    if (find_among_b(a_14) == 0) {
      return false;
    }
    bra = cursor;
    if (!(limit >= 4)) {
      return false;
    }
    slice_del();
    return true;
  }

  private boolean r_Suffix_Noun_Step2c2() {
    ket = cursor;
    if (find_among_b(a_15) == 0) {
      return false;
    }
    bra = cursor;
    if (!(limit >= 4)) {
      return false;
    }
    slice_del();
    return true;
  }

  private boolean r_Suffix_Noun_Step3() {
    ket = cursor;
    if (find_among_b(a_16) == 0) {
      return false;
    }
    bra = cursor;
    if (!(limit >= 3)) {
      return false;
    }
    slice_del();
    return true;
  }

  private boolean r_Suffix_Verb_Step1() {
    int among_var;
    ket = cursor;
    among_var = find_among_b(a_17);
    if (among_var == 0) {
      return false;
    }
    bra = cursor;
    switch (among_var) {
      case 1:
        if (!(limit >= 4)) {
          return false;
        }
        slice_del();
        break;
      case 2:
        if (!(limit >= 5)) {
          return false;
        }
        slice_del();
        break;
      case 3:
        if (!(limit >= 6)) {
          return false;
        }
        slice_del();
        break;
    }
    return true;
  }

  private boolean r_Suffix_Verb_Step2a() {
    int among_var;
    ket = cursor;
    among_var = find_among_b(a_18);
    if (among_var == 0) {
      return false;
    }
    bra = cursor;
    switch (among_var) {
      case 1:
        if (!(limit >= 4)) {
          return false;
        }
        slice_del();
        break;
      case 2:
        if (!(limit >= 5)) {
          return false;
        }
        slice_del();
        break;
      case 3:
        if (!(limit > 5)) {
          return false;
        }
        slice_del();
        break;
      case 4:
        if (!(limit >= 6)) {
          return false;
        }
        slice_del();
        break;
    }
    return true;
  }

  private boolean r_Suffix_Verb_Step2b() {
    ket = cursor;
    if (find_among_b(a_19) == 0) {
      return false;
    }
    bra = cursor;
    if (!(limit >= 5)) {
      return false;
    }
    slice_del();
    return true;
  }

  private boolean r_Suffix_Verb_Step2c() {
    int among_var;
    ket = cursor;
    among_var = find_among_b(a_20);
    if (among_var == 0) {
      return false;
    }
    bra = cursor;
    switch (among_var) {
      case 1:
        if (!(limit >= 4)) {
          return false;
        }
        slice_del();
        break;
      case 2:
        if (!(limit >= 6)) {
          return false;
        }
        slice_del();
        break;
    }
    return true;
  }

  private boolean r_Suffix_All_alef_maqsura() {
    ket = cursor;
    if (find_among_b(a_21) == 0) {
      return false;
    }
    bra = cursor;
    slice_from("\u064A");
    return true;
  }

  @Override
  public boolean stem() {
    B_is_noun = true;
    B_is_verb = true;
    B_is_defined = false;
    int v_1 = cursor;
    r_Checks1();
    cursor = v_1;
    r_Normalize_pre();
    limit_backward = cursor;
    cursor = limit;
    int v_3 = limit - cursor;
    lab0:
    {
      lab1:
      {
        int v_4 = limit - cursor;
        lab2:
        {
          if (!(B_is_verb)) {
            break lab2;
          }
          lab3:
          {
            int v_5 = limit - cursor;
            lab4:
            {
              {
                int v_6 = 1;
                while (true) {
                  int v_7 = limit - cursor;
                  lab5:
                  {
                    if (!r_Suffix_Verb_Step1()) {
                      break lab5;
                    }
                    v_6--;
                    continue;
                  }
                  cursor = limit - v_7;
                  break;
                }
                if (v_6 > 0) {
                  break lab4;
                }
              }
              lab6:
              {
                int v_8 = limit - cursor;
                lab7:
                {
                  if (!r_Suffix_Verb_Step2a()) {
                    break lab7;
                  }
                  break lab6;
                }
                cursor = limit - v_8;
                lab8:
                {
                  if (!r_Suffix_Verb_Step2c()) {
                    break lab8;
                  }
                  break lab6;
                }
                cursor = limit - v_8;
                if (cursor <= limit_backward) {
                  break lab4;
                }
                cursor--;
              }
              break lab3;
            }
            cursor = limit - v_5;
            lab9:
            {
              if (!r_Suffix_Verb_Step2b()) {
                break lab9;
              }
              break lab3;
            }
            cursor = limit - v_5;
            if (!r_Suffix_Verb_Step2a()) {
              break lab2;
            }
          }
          break lab1;
        }
        cursor = limit - v_4;
        lab10:
        {
          if (!(B_is_noun)) {
            break lab10;
          }
          int v_9 = limit - cursor;
          lab11:
          {
            lab12:
            {
              int v_10 = limit - cursor;
              lab13:
              {
                if (!r_Suffix_Noun_Step2c2()) {
                  break lab13;
                }
                break lab12;
              }
              cursor = limit - v_10;
              lab14:
              {
                lab15:
                {
                  if (!(B_is_defined)) {
                    break lab15;
                  }
                  break lab14;
                }
                if (!r_Suffix_Noun_Step1a()) {
                  break lab14;
                }
                lab16:
                {
                  int v_12 = limit - cursor;
                  lab17:
                  {
                    if (!r_Suffix_Noun_Step2a()) {
                      break lab17;
                    }
                    break lab16;
                  }
                  cursor = limit - v_12;
                  lab18:
                  {
                    if (!r_Suffix_Noun_Step2b()) {
                      break lab18;
                    }
                    break lab16;
                  }
                  cursor = limit - v_12;
                  lab19:
                  {
                    if (!r_Suffix_Noun_Step2c1()) {
                      break lab19;
                    }
                    break lab16;
                  }
                  cursor = limit - v_12;
                  if (cursor <= limit_backward) {
                    break lab14;
                  }
                  cursor--;
                }
                break lab12;
              }
              cursor = limit - v_10;
              lab20:
              {
                if (!r_Suffix_Noun_Step1b()) {
                  break lab20;
                }
                lab21:
                {
                  int v_13 = limit - cursor;
                  lab22:
                  {
                    if (!r_Suffix_Noun_Step2a()) {
                      break lab22;
                    }
                    break lab21;
                  }
                  cursor = limit - v_13;
                  lab23:
                  {
                    if (!r_Suffix_Noun_Step2b()) {
                      break lab23;
                    }
                    break lab21;
                  }
                  cursor = limit - v_13;
                  if (!r_Suffix_Noun_Step2c1()) {
                    break lab20;
                  }
                }
                break lab12;
              }
              cursor = limit - v_10;
              lab24:
              {
                lab25:
                {
                  if (!(B_is_defined)) {
                    break lab25;
                  }
                  break lab24;
                }
                if (!r_Suffix_Noun_Step2a()) {
                  break lab24;
                }
                break lab12;
              }
              cursor = limit - v_10;
              if (!r_Suffix_Noun_Step2b()) {
                cursor = limit - v_9;
                break lab11;
              }
            }
          }
          if (!r_Suffix_Noun_Step3()) {
            break lab10;
          }
          break lab1;
        }
        cursor = limit - v_4;
        if (!r_Suffix_All_alef_maqsura()) {
          break lab0;
        }
      }
    }
    cursor = limit - v_3;
    cursor = limit_backward;
    int v_15 = cursor;
    lab26:
    {
      int v_16 = cursor;
      lab27:
      {
        if (!r_Prefix_Step1()) {
          cursor = v_16;
          break lab27;
        }
      }
      int v_17 = cursor;
      lab28:
      {
        if (!r_Prefix_Step2()) {
          cursor = v_17;
          break lab28;
        }
      }
      lab29:
      {
        int v_18 = cursor;
        lab30:
        {
          if (!r_Prefix_Step3a_Noun()) {
            break lab30;
          }
          break lab29;
        }
        cursor = v_18;
        lab31:
        {
          if (!(B_is_noun)) {
            break lab31;
          }
          if (!r_Prefix_Step3b_Noun()) {
            break lab31;
          }
          break lab29;
        }
        cursor = v_18;
        if (!(B_is_verb)) {
          break lab26;
        }
        int v_19 = cursor;
        lab32:
        {
          if (!r_Prefix_Step3_Verb()) {
            cursor = v_19;
            break lab32;
          }
        }
        if (!r_Prefix_Step4_Verb()) {
          break lab26;
        }
      }
    }
    cursor = v_15;
    r_Normalize_post();
    return true;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof ArabicStemmer;
  }

  @Override
  public int hashCode() {
    return ArabicStemmer.class.getName().hashCode();
  }
}