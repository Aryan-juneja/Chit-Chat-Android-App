export default {
  env: {
    es6: true,     // Enables ES6 features
    node: true,    // Recognizes Node.js global variables
    mocha: true,   // If you run tests with Mocha, add this to allow Mocha globals
  },
  parserOptions: {
    ecmaVersion: 2018, // Use ECMAScript 2018 features
  },
  extends: [
    "eslint:recommended", // Use recommended ESLint rules
    "google",             // Use Google's style guide
  ],
  rules: {
    "no-restricted-globals": ["error", "name", "length"], // Restricts the use of certain global variables
    "prefer-arrow-callback": "error", // Enforces the use of arrow functions
    "quotes": ["error", "double", { "allowTemplateLiterals": true }], // Enforces double quotes
    "no-undef": "error", // Prevents the use of undeclared variables
    "object-curly-spacing": ["error", "never"], // No spaces inside curly braces
    "indent": ["error", 2], // Enforces 2-space indentation
    "comma-dangle": ["error", "always-multiline"], // Requires trailing commas for multiline
  },
  overrides: [
    {
      files: ["**/*.spec.*"], // Targeting test files
      env: {
        mocha: true, // Allow Mocha globals in test files
      },
      rules: {}, // You can add specific rules for test files here if needed
    },
  ],
  globals: {
    // If you have any global variables to declare, add them here
  },
};
