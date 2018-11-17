let _nixpkgs = import <nixpkgs>{};
    pkgs = import (_nixpkgs.fetchFromGitHub {
      owner = "NixOS";
      repo = "nixpkgs";
      rev = "4fe3edd7253c5286ac7a2c54304f055d22a3c744";
      sha256 = "17hdv3n5gcrhhmjm0vzbzmgrdczzidzikbz1s1y93y98yfnah4si";
    }) {};
    myPython3 = pkgs.python3.withPackages(ps: [
      ps.neovim
    ]);
    myNeovim =
      let pythonDeps = x: with x; [websocket_client sexpdata neovim greenlet ];
      in pkgs.neovim.override {
        extraPythonPackages = pythonDeps(pkgs.python2Packages);
        extraPython3Packages = pythonDeps(pkgs.python3Packages);
      };
in pkgs.stdenv.mkDerivation {
  name = "dev-env";
  buildInputs = with pkgs; let pp = pkgs.python3Packages; in [
    sbt
    myNeovim
    myPython3
  ];

  BROWSER = "firefox";

  SBT_OPTS = "-Xss2M -XX:MaxMetaspaceSize=1024m";
}
