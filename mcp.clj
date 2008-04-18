
;   Copyright (c) Martin Jackson. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php)
;   which can be found in the file CPL.TXT at the root of the clojure distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(import '(java.lang ClassLoader)) 
(import '(java.net URL URLClassLoader)) 
(import '(java.io File))
(clojure/refer 'clojure)


; defn- makes this private function definition -- this file only
(defn- ext? [s] #(.. % (getName) (endsWith s)))

(def jar?  (ext? ".jar"))
(def html? (ext? ".html"))
(def xml?  (ext? ".xml"))

(defn list-files 
" List the files in a directory
HOW TO USE: (list-files jar?  \"/home/martin_jackson/Programming/libs/\") "
[pred dirpath]
 (filter pred (. (new java.io.File dirpath) (listFiles))))

(defn find-in-seq [pred s] (some #(if (pred %) % nil) s))

(defn member? [collection x]
  (if (some (fn [item] (= x item)) collection)
          true
          false))

(defn listClasspath []
  (seq (.. java.lang.ClassLoader (getSystemClassLoader) (getURLs))))
  
(defn addUrlToClasspath [#^java.net.URL url]
  (let [ sysloader    (. java.lang.ClassLoader (getSystemClassLoader))
         methods      (.. Class (forName "java.net.URLClassLoader") (getDeclaredMethods)) 
         addURLMethod (find-in-seq #(= "addURL" (. % (getName)) ) methods) ] 
    (when-not (member? (listClasspath) url)   ;; dont add if it is already in classpath 
	    (doto addURLMethod 
		  (setAccessible true)
		  (invoke sysloader (to-array [url])))
		true)))
  
(defn addFileToClasspath [#^java.io.File f]
  (addUrlToClasspath (.. f (toURI) (toURL))))
  
(defn addFilePathToClasspath [#^String s]
  (addFileToClasspath (new File s)))

(defn addAllJarsToClasspath [dir]
   (doseq f (list-files jar? dir) (addFilePathToClasspath (str f))))

;; Test cases
(comment

(if (addFilePathToClasspath "/home/martin_jackson/Programming/libs/javacsv.jar")  (println "Dynamically added javacsv.jar"))
(prn "CLASSPATH" (listClasspath))

(def testData (list "/home/martin_jackson/Programming/libs/javacsv.jar" "/home/martin_jackson/Programming/libs/SuperCSV-1.31.jar"))
(doseq xx testData  (if (addFilePathToClasspath (str xx))  (println "Dynamically added " (str xx) " to classLoader")))
(prn "CLASSPATH" (listClasspath))

(prn "add all jars in a directory")
(prn "list files: " (list-files jar?  "/home/martin_jackson/Programming/libs/"))
(addAllJarsToClasspath "/home/martin_jackson/Programming/libs/")
(prn "CLASSPATH" (listClasspath))

)
