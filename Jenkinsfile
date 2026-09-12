// =============================================================================
// Jenkins Declarative Pipeline for Hepsiburada QA Automation (qa-project-101)
// =============================================================================

pipeline {
    // Execute on any available Jenkins agent
    agent any

    // Configurable runtime parameters for parameterized test execution
    parameters {
        choice(name: 'BROWSER', choices: ['chrome', 'firefox'], description: 'Target browser for execution')
        choice(name: 'HEADLESS', choices: ['true', 'false'], description: 'Run browser in headless mode')
        string(name: 'THREAD_COUNT', defaultValue: '3', description: 'Number of parallel execution threads')
        string(name: 'SUITE_XML', defaultValue: 'testng.xml', description: 'TestNG Suite XML file to run')
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
        pollSCM('H/2 * * * *')
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
                echo "Starting test suite execution with Browser=${params.BROWSER}, Headless=${params.HEADLESS}, Threads=${params.THREAD_COUNT}"
                
                // Execute tests against remote Selenium Grid with dynamic parameters
                sh """
                    mvn clean test \
                        -Dbrowser=${params.BROWSER} \
                        -Dheadless=${params.HEADLESS} \
                        -Dremote=true \
                        -DgridUrl=${SELENIUM_REMOTE_URL} \
                        -Dsurefire.suiteXmlFiles=${params.SUITE_XML}
                """
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
