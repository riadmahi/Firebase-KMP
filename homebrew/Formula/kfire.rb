class Kfire < Formula
  desc "Firebase CLI for Kotlin Multiplatform projects"
  homepage "https://github.com/riadmahi/kfire"
  url "https://github.com/riadmahi/kfire/releases/download/v1.0.0/kfire-1.0.0.tar.gz"
  sha256 "REPLACE_WITH_SHA256"
  license "Apache-2.0"

  depends_on "openjdk@17"

  def install
    libexec.install Dir["*"]

    # Create wrapper script that sets JAVA_HOME
    (bin/"kfire").write <<~EOS
      #!/bin/bash
      export JAVA_HOME="#{Formula["openjdk@17"].opt_prefix}"
      exec "#{libexec}/bin/kfire" "$@"
    EOS
  end

  test do
    assert_match "kfire", shell_output("#{bin}/kfire --help")
  end
end
