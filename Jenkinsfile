// =============================================================================
// Jenkins Declarative Pipeline for Hepsiburada QA Automation (qa-project-101)
// =============================================================================

pipeline {
    // Execute on any available Jenkins agent
    agent any

    // Configurable runtime parameters for parameterized test execution
    parameters {
        choice(name: 'EXECUTION_TYPE', choices: ['MOBILE_WEB', 'WEB_UI', 'API'], description: 'Koşturulacak Test Türü')
        choice(name: 'SUITE_XML', choices: [
            'src/test/resources/suites/mobile-smoke-tests.xml',
            'src/test/resources/suites/mobile-regression-tests.xml',
            'src/test/resources/suites/smoke-tests.xml',
            'src/test/resources/suites/regression-tests.xml',
            'src/test/resources/suites/api-tests.xml'
        ], description: 'TestNG Suite XML file to run')
        choice(name: 'BROWSER', choices: ['chrome', 'firefox'], description: 'Web UI: Target browser for execution')
        choice(name: 'HEADLESS', choices: ['true', 'false'], description: 'Web UI: Run browser in headless mode')
        string(name: 'DEVICE_NAME', defaultValue: 'Medium_Phone', description: 'Mobile: Target device / emulator')
        string(name: 'APPIUM_SERVER_URL', defaultValue: 'http://192.168.1.106:4723/', description: 'Mobile: Appium Server URL')
        string(name: 'THREAD_COUNT', defaultValue: '2', description: 'Number of parallel execution threads')
    }

    // Global environment variables
    environment {
        // Selenium Grid Hub endpoint in the Docker network
        SELENIUM_REMOTE_URL = 'http://selenium-hub:4444'
    }

    // Tools configuration - Automatically installs and configures Maven
    tools {
        maven 'Maven_3.9.6'
    }

    // Trigger pipeline automatically every 3 minutes for continuous testing
    triggers {
        pollSCM('H/5 * * * *')
    }

    // Pipeline Execution Stages
    stages {
        // -------------------------------------------------------------
        // Stage 1: Checkout Source Code from GitHub
        // -------------------------------------------------------------
        stage('Checkout Source') {
            steps {
                echo 'Checking out latest test automation code from Git repository...'
                checkout scm
            }
        }

        // -------------------------------------------------------------
        // Stage 2: Verify Build Environment (Java & Maven)
        // -------------------------------------------------------------
        stage('Environment Check') {
            steps {
                echo 'Verifying Java and Maven runtime versions...'
                sh 'java -version'
                sh 'mvn -version'
            }
        }

        // -------------------------------------------------------------
        // Stage 3: Execute Test Suite with Maven
        // -------------------------------------------------------------
        stage('Execute Automated Tests') {
            steps {
                script {
                    if (params.EXECUTION_TYPE == 'MOBILE_WEB') {
                        echo "📱 Starting Mobile Web tests against Appium: ${params.APPIUM_SERVER_URL} with Suite=${params.SUITE_XML}"
                        sh """
                            mvn clean test \
                                -DsuiteXmlFile=${params.SUITE_XML} \
                                -Dappium.server.url=${params.APPIUM_SERVER_URL} \
                                -Dmobile.android.device.name="${params.DEVICE_NAME}"
                        """
                    } else if (params.EXECUTION_TYPE == 'API') {
                        echo "🧪 Starting API Automated Tests with Suite=${params.SUITE_XML}"
                        sh """
                            mvn clean test \
                                -DsuiteXmlFile=${params.SUITE_XML}
                        """
                    } else {
                        echo "🌐 Starting Web UI tests against Selenium Grid with Browser=${params.BROWSER}, Headless=${params.HEADLESS}"
                        sh """
                            mvn clean test \
                                -Dbrowser=${params.BROWSER} \
                                -Dheadless=${params.HEADLESS} \
                                -Dremote=true \
                                -DgridUrl=${SELENIUM_REMOTE_URL} \
                                -DsuiteXmlFile=${params.SUITE_XML}
                        """
                    }
                }
            }
        }
    }

    // Post-Execution Actions
    post {
        always {
            echo 'Archiving Allure test results and generating report...'
            
            // Generate Allure Report from target/allure-results
            allure([
                includeProperties: false,
                jdk: '',
                properties: [],
                reportBuildPolicy: 'ALWAYS',
                results: [[path: 'target/allure-results']]
            ])

            // Archive standard JUnit/Surefire XML results
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
        }
        success {
            echo 'All automated tests completed successfully! 🚀'
        }
        failure {
            echo 'Automated tests failed! Please check Allure report for failure details. ❌'
        }
    }
}
