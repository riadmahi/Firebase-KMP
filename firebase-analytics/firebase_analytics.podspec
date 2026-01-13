Pod::Spec.new do |spec|
    spec.name                     = 'firebase_analytics'
    spec.version                  = '1.0.0'
    spec.homepage                 = 'https://github.com/riadmahi/kfire'
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'Firebase Analytics KMP wrapper'
    spec.vendored_frameworks      = 'build/cocoapods/framework/FirebaseAnalytics.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target    = '15.0'
    spec.dependency 'FirebaseAnalytics', '11.6.0'
    if !Dir.exist?('build/cocoapods/framework/FirebaseAnalytics.framework') || Dir.empty?('build/cocoapods/framework/FirebaseAnalytics.framework')
        raise "
        Kotlin framework 'FirebaseAnalytics' doesn't exist yet, so a proper Xcode project can't be generated.
        'pod install' should be executed after running ':generateDummyFramework' Gradle task:
            ./gradlew :firebase-analytics:generateDummyFramework
        Alternatively, proper pod installation is performed during Gradle sync in the IDE (if Podfile location is set)"
    end
    spec.xcconfig = {
        'ENABLE_USER_SCRIPT_SANDBOXING' => 'NO',
    }
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':firebase-analytics',
        'PRODUCT_MODULE_NAME' => 'FirebaseAnalytics',
    }
    spec.script_phases = [
        {
            :name => 'Build firebase_analytics',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                    echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
                    exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
end
