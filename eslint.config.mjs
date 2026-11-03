// ESLint flat config for the frontend JS. Only core rules are used, because CI installs
// eslint globally and a global install can't resolve shareable configs or plugins.
export default [
  {
    files: ["app/src/main/resources/static/js/**/*.js"],
    languageOptions: {
      ecmaVersion: "latest",
      sourceType: "module"
    },
    rules: {
      "no-dupe-keys": "error",
      "no-duplicate-case": "error",
      "no-unreachable": "error",
      "no-unused-vars": "warn",
      "no-redeclare": "error",
      "no-self-assign": "error",
      "eqeqeq": ["warn", "smart"]
    }
  }
];
