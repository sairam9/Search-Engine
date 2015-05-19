import java.util.regex.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PorterStemmer {

   // a single consonant
   private static final String c = "[^aeiou]";
   // a single vowel
   private static final String v = "[aeiouy]";

   // a sequence of consonants; the second/third/etc consonant cannot be 'y'
   private static final String C = c + "[^aeiouy]*";
   // a sequence of vowels; the second/third/etc cannot be 'y'
   private static final String V = v + "[aeiou]*";

   // this regex pattern tests if the token has measure > 0 [at least one VC].
//   private static final Pattern mGr0 = Pattern.compile("^(" + C + ")?" + V + C);
   private static final Pattern mGr0 = Pattern.compile("^(" + C + ")?" + "(" + V + C + ")+(" + V + ")?$");
   //private static final Pattern mGr01 = Pattern.compile("(" + c + ")*(" + V + ")+(" + C + ")+("+ v + ")?(" +c + ")?");
   private static final Pattern mGr11 = Pattern.compile("^[^aeiou]*[aeiou]+[^aeiou]+");
   
   // step 1a.3: remove "s" if word ends with "s" but do nothing if it ends with "ss".
   private static final Pattern endSS = Pattern.compile("ss$");

   // add more Pattern variables for the following patterns:
   // m equals 1: token has measure == 1
   // m greater than 1: token has measure > 1
   // vowel: token has a vowel after the first (optional) C
   // double consonant: token ends in two consonants that are the same, 
   //			unless they are L, S, or Z. (look up "backreferencing" to help 
   //			with this)
   // m equals 1, Cvc: token is in Cvc form, where the last c is not w, x,
   //			or y.
   
   // svs = *v* -- vowel
   private static final Pattern svs = Pattern.compile("^(" + C + ")?" + V);
   private static final Pattern svs1 = Pattern.compile("[^aeiou]*[aeiou]+[^aeiou]*");
   private static final Pattern svs2 = Pattern.compile("^(" + C + ")*" + v + "(" + C + ")*");
   
   // step 1b*: (*d and not (*L or *S or *Z))
   private static final Pattern doubleCon = Pattern.compile("([^aeioulsz])\\1$");
//   private static final Pattern doubleCon00 = Pattern.compile("([^aeioulsz]{2})$");
   private static final Pattern doubleCon1 = Pattern.compile("(" + C + ")?" + "(" + V + ")?([^aeioulsz]{2}$)");
   
   // step 1b*: m = 1 and *o, m1sd
   private static final Pattern m1so = Pattern.compile( C + v + "[^aieouwxy]$" );
   private static final Pattern mEq1 = Pattern.compile("^(" + C + ")?" + "(" + V + C + ")(" + V + ")?$");
   private static final Pattern staro = Pattern.compile("(" + C + v + "[^aeiouwxy])$");
   
   // step 4 and 5: m is at least 2
   private static final Pattern mGr2 = Pattern.compile("^(" + C + ")?" + V + C + V + C);
   
   public static String processToken(String token) {
      if (token.length() < 3) {
         return token; // token must be at least 3 chars
      }
      // step 1a
      if (token.endsWith("sses")) {
//          System.out.println("step 1a.");
         token = token.substring(0, token.length() - 2);
      }
      // program the other steps in 1a. 
      // note that Step 1a.3 implies that there is only a single 's' as the 
      //	suffix; ss does not count. you may need a regex pattern here for 
      // "not s followed by s".

      else if (token.endsWith("ies")) {
          token = token.substring(0, token.length() - 2);
      }
      else if (token.endsWith("s")) {
          if (!endSS.matcher(token).find()) {
              token = token.substring(0, token.length() - 1);
          }
      }
      
      // step 1b
      boolean doStep1bb = false;
      //		step 1b
      if (token.endsWith("eed")) { // 1b.1
         // token.substring(0, token.length() - 3) is the stem prior to "eed".
         // if that has m>0, then remove the "d".
         String stem = token.substring(0, token.length() - 3);
         if (mGr0.matcher(stem).find()) { // if the pattern matches the stem
            token = stem + "ee";
//            doStep1bb = true;
         }
      }
      // program the rest of 1b. set the boolean doStep1bb to true if Step 1b* 
      // should be performed.
      else if (token.endsWith("ed")) { //1b.2
          if (svs.matcher(token.substring(0, token.length() - 2)).find()) {
              token = token.substring(0, token.length() - 2);
              doStep1bb = true;
          }
      }
      else if (token.endsWith("ing")) { //1b.3
          String stem = token.substring(0, token.length() - 3);
//          System.out.println("1b last else if " + stem + " 1b last else if - else svs: " + svs.matcher(stem).find() + " svs1:" + svs1.matcher(stem).find() + svs2.matcher(stem).find());
          if (svs1.matcher(stem).find()) {
//              System.out.println("1b last else if - else svs: " + svs1.matcher(stem).find());
              token = stem;
              doStep1bb = true;
          }
      }
      
      // step 1b*, only if the 1b.2 or 1b.3 were performed.
      if (doStep1bb) {
//          System.out.println("doubleCon: " + doubleCon.matcher(token).find() + "doubleCon1" + doubleCon1.matcher(token).find());
         if (token.endsWith("at") || token.endsWith("bl")
          || token.endsWith("iz")) {

            token = token + "e";
         }
         // use the regex patterns you wrote for 1b*.4 and 1b*.5
         else if (doubleCon1.matcher(token).find()) {
             token = token.substring(0,token.length() - 1);
         }
         else if (m1so.matcher(token).find()) {
             token = token.concat("e");
         }         
      }

      // step 1c
      // program this step. test the suffix of 'y' first, then test the 
      // condition *v*.

      if (token.endsWith("y")) {
          if (svs.matcher(token.substring(0, token.length() - 1)).find()) {
              token = token.substring(0, token.length() - 1) + "i";
          }
      }

      // step 2
      // program this step. for each suffix, see if the token ends in the 
      // suffix. 
      //		* if it does, extract the stem, and do NOT test any other suffix.
      //    * take the stem and make sure it has m > 0.
      //			* if it does, complete the step. if it does not, do not 
      //				attempt any other suffix.
      // you may want to write a helper method for this. a matrix of 
      // "suffix"/"replacement" pairs might be helpful. It could look like
      // string[][] step2pairs = {  new string[] {"ational", "ate"}, 
      //										new string[] {"tional", "tion"}, ....
      token = step2(token);

      // step 3
      // program this step. the rules are identical to step 2 and you can use
      // the same helper method. you may also want a matrix here.
      token = step3(token);
      
      // step 4
      // program this step similar to step 2/3, except now the stem must have
      // measure > 1.
      // note that ION should only be removed if the suffix is SION or TION, 
      // which would leave the S or T.
      // as before, if one suffix matches, do not try any others even if the 
      // stem does not have measure > 1.
      token = step4(token);

      // step 5
      // program this step. you have a regex for m=1 and for "Cvc", which
      // you can use to see if m=1 and NOT Cvc.
      // all your code should change the variable token, which represents
      // the stemmed term for the token.
      token = step5(token);

      return token;
   }
   
   public static String step2 (String token) {
       String [][] step2pair = {new String [] {"ational", "ate"}, new String [] {"tional", "tion"}, new String [] {"enci", "ence"}, new String [] {"anci", "ance"}, new String [] {"izer", "ize"}, new String [] {"bli", "ble"}, new String [] {"alli", "al"}, new String [] {"entli", "ent"}, new String [] {"eli", "e"}, new String [] {"ousli", "ous"}, new String [] {"ization", "ize"}, new String [] {"ation", "ate"}, new String [] {"ator", "ate"}, new String [] {"alism", "al"}, new String [] {"iveness", "ive"}, new String [] {"fulness", "ful"}, new String [] {"ousness", "ous"}, new String [] {"aliti", "al"}, new String [] {"aviti", "ive"}, new String [] {"biliti", "ble"}};
       
       for(String [] arr: step2pair) {
           if (token.endsWith(arr[0])) {
               String stem = token.substring(0, token.length() - arr[0].length());
//               System.out.println("step 2 first if " + token + arr[0] + arr[1] + stem);
//               System.out.println(mGr01.matcher(stem).matches() + " mGr01 " + mGr0.matcher(stem).matches() + "mGr0 " + mGr11.matcher(stem).matches() + " mGr11");
               if (mGr0.matcher(stem).find()) {
                   token = stem.concat(arr[1]);
//                   System.out.println("step 2 token: " + token);
                   break;
               }
               else {
//                   System.out.println("else step 2");
                   break;
               }

           }
           
       }
       return token;
   }
   
   public static String step3 (String token) {
       String [][] step3pair = {new String [] {"icate", "ic"}, new String [] {"ative", ""}, new String [] {"alize", "al"}, new String [] {"iciti", "ic"}, new String [] {"ical", "ic"}, new String [] {"ful", ""}, new String [] {"ness", ""}};
       
       for (String [] arr: step3pair) {
               if (token.endsWith(arr[0])) {
                   String stem = token.substring(0, token.length() - arr[0].length());
                   if (mGr0.matcher(stem).find()) {
                       token = stem.concat(arr[1]);
//                       System.out.println("step 3 token: " + token);
                       break;
                   }
               }
           }
       
       return token;
   }
   
   public static String step4 (String token) {
       String [] step4 = new String [] {"al", "anc", "ence", "er", "ic", "able", "ible", "ant", "ement", "ment", "ent", "ion", "ou", "ism", "ate", "iti", "ous", "ive", "ize"};
       
       for (String temp: step4) {
           if (token.endsWith(temp)) {
               String stem = token.substring(0, token.length() - temp.length());               
//               System.out.println("step4 stem: " + stem + " mGr2: " + mGr2.matcher(stem).find());
               if (mGr2.matcher(stem).find()) {
//                   System.out.println("mGr2: " + mGr2.matcher(stem).find()+ stem);
                   if (temp.equalsIgnoreCase("ion")) {
                       if (stem.endsWith("s") || stem.endsWith("t")) {
                           token = stem;
//                           System.out.println("step 4 first if token: " + token);
                           break;
                       }
                   }
//                   else if (temp.equalsIgnoreCase("ll")) {
//                       token = stem.substring(0, stem.length() - 1);
//                       System.out.println("step 4-5 else if token: " + token);
//                       break;
//                   }
                   else {
                       token = stem;
//                       System.out.println("step 4 else token: " + token);                   
                       break;
                   }
               }
//               else if (mGr0.matcher(stem).matches() && !(m1so.matcher(stem).matches())) {
//                   if (stem.endsWith("e")) {
//                       token = stem.substring(0, stem.length() - 1);
//                       System.out.println("step 4-5 last else if token: " + token);
//                       break;
//                   }
//               }
               else break;
           }
       }
       return token;
   }
   
   public static String step5 (String token) {
       if (token.endsWith("e")) {
           String stem = token.substring(0, token.length() - 1);
           if (mGr2.matcher(stem).find()) {
               token = stem;
           }
           else if (mEq1.matcher(stem).find() && !staro.matcher(stem).find()) {
               token = stem;
           }
//           System.out.println("step 5.");
       }
       else if (token.endsWith("ll")) {
           String stem = token.substring(0, token.length() - 2);
           if (mGr2.matcher(stem).find()) { 
               token = stem + "l";
//               System.out.println("step 5.");
           }
       }
       return token;
   }
   
   public static void main (String [] args) {
       DataInputStream dis = new DataInputStream(System.in);
       String word = "";
       while (true) {
           System.out.println("Enter a word to perform Porter Stemming or \"quit\" to quit:");
           
           try {
               word = dis.readLine();
               if (word.equalsIgnoreCase("quit")) {
                   break;
               }
           } catch (IOException ex) {
               Logger.getLogger(PorterStemmer.class.getName()).log(Level.SEVERE, null, ex);
           }
           
           String result = processToken(word);
           System.out.println("The stem of the word \"" + word + "\" is: " + result);
       }
   }
}
