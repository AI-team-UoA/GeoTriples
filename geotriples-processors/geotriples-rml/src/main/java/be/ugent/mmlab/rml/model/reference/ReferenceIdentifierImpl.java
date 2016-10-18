package be.ugent.mmlab.rml.model.reference;

/**
 * 
 * This class holds an expression that refers to a certain value.
 * It has lost most of its use, maybe replace it?
 *
 * @author mielvandersande
 */
public class ReferenceIdentifierImpl implements ReferenceIdentifier {

        private String reference = null;

        private ReferenceIdentifierImpl(String reference) {
                this.reference = reference;
        }

        /**
         * Build a Reference Identifier from a RML config file.
         *
         * @param referenceName The reference.
         * @return
         */
        public static ReferenceIdentifierImpl buildFromR2RMLConfigFile(String reference) {
                if (reference == null) {
                        return null;
                }

                // Be optimist...
                return new ReferenceIdentifierImpl(reference);
        }

        public String replaceAll(String input, String replaceValue) {
                // Try simple replace...
                String localResult = input.replaceAll("\\{" + reference + "\\}",
                        replaceValue);
                // Must have replaced something
                assert !localResult.equals(input) : ("Impossible to replace "
                        + reference + " in " + input);
                return localResult;
        }

        @Override
        public String toString() {
                return reference;
        }
}
