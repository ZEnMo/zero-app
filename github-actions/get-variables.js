


/**
 * @typedef BuildVariables
 * @type {object}
 * @property {string} ZTOR_PR_CONTAINER_APP_NAME - Azure Container Apps name for Pull Request environment
 * @property {string} VERSION_TAG - tag name for containers, libraries, etc
 */

/**
 * Called by @actions/github-script to get the variables for the build.
 *
 * @param {import('@actions/github/lib/context').Context} context
 * @param {object} env
 *
 * @returns {BuildVariables}
 */
module.exports = (context) => {
    const commit = context.payload.pull_request?.head?.sha ?? context.sha
    const shortCommit = commit.substring(0, 7)

    const containerAppBaseName = 'ztor'
    const branch = context.payload.pull_request?.head?.ref ?? context.ref.match(/refs\/heads\/(.+)/)[1]
    // Azure Container Apps name can be max 32 characters
    const maxBranchLength = 32 - `${context.runNumber}`.length - containerAppBaseName.length - 2 * '-'.length
    const shortBranch = branch
        .toLowerCase()
        .substring(0, maxBranchLength)
        .replaceAll(/-*$/g, '') // remove trailing dashes because it would lead to an invalid name

    const versionIdentifier = `${shortBranch}-${context.runNumber}`

    return {
        ZTOR_PR_CONTAINER_APP_NAME:  `${containerAppBaseName}-${versionIdentifier}`,
        VERSION_TAG: `${versionIdentifier}-${shortCommit}`,
        DOCKER_STACK_NAME: `zero-${getEnvironment(context)}`,
        GITHUB_ENVIRONMENT: `swarm-${getEnvironment(context)}`,
        // handled by wildcard certificate
        FRONTEND_HOSTNAME: `frontend-${versionIdentifier}.zero.zenmo.com`,
        ZTOR_HOSTNAME: `ztor-${versionIdentifier}.zero.zenmo.com`,
    }
}

const branchToEnvironment = {
    production: 'production',
    main: 'test',
}

/**
 * Get environment name which is used for variable and secret management in GitHub Actions.
 *
 * @param {import('@actions/github/lib/context').Context} context
 *
 * @returns {'production' | 'test' | 'pullrequest'}
 */
function getEnvironment(context) {
    if (context.payload.pull_request) {
        return 'pullrequest'
    }

    const branch = context.ref.match(/refs\/heads\/(.+)/)[1]

    return branchToEnvironment[branch] ?? 'pullrequest'
}
