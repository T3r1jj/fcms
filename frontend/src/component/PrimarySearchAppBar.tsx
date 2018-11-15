import AppBar, {AppBarProps} from '@material-ui/core/AppBar';
import Badge from '@material-ui/core/Badge';
import IconButton from '@material-ui/core/IconButton';
import InputBase from '@material-ui/core/InputBase';
import Menu from '@material-ui/core/Menu';
import MenuItem from '@material-ui/core/MenuItem';
import {StyleRulesCallback, withStyles} from '@material-ui/core/styles';
import {fade} from '@material-ui/core/styles/colorManipulator';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import CallbackIcon from '@material-ui/icons/CallMissed';
import HomeIcon from '@material-ui/icons/Home';
import MoreIcon from '@material-ui/icons/MoreVert';
import NotificationsIcon from '@material-ui/icons/Notifications';
import SearchIcon from '@material-ui/icons/Search';
import React from 'react';
import {Link} from "react-router-dom";

const styles: StyleRulesCallback = theme => ({
    grow: {
        flexGrow: 1,
    },
    inputInput: {
        paddingBottom: theme.spacing.unit,
        paddingLeft: theme.spacing.unit * 10,
        paddingRight: theme.spacing.unit,
        paddingTop: theme.spacing.unit,
        transition: theme.transitions.create('width'),
        width: '100%',
        [theme.breakpoints.up('md')]: {
            width: 200,
        },
    },
    inputRoot: {
        color: 'inherit',
        width: '100%',
    },
    linkWrapper: {
        color: 'inherit'
    },
    menuButton: {
        marginLeft: -12,
        marginRight: 20,
    },
    root: {
        width: '100%',
    },
    search: {
        '&:hover': {
            backgroundColor: fade(theme.palette.common.white, 0.25),
        },
        backgroundColor: fade(theme.palette.common.white, 0.15),
        borderRadius: theme.shape.borderRadius,
        marginLeft: 0,
        marginRight: theme.spacing.unit * 2,
        position: 'relative',
        width: '100%',
        [theme.breakpoints.up('sm')]: {
            marginLeft: theme.spacing.unit * 3,
            width: 'auto',
        },
    },
    searchIcon: {
        alignItems: 'center',
        display: 'flex',
        height: '100%',
        justifyContent: 'center',
        pointerEvents: 'none',
        position: 'absolute',
        width: theme.spacing.unit * 9,
    },
    sectionDesktop: {
        display: 'none',
        [theme.breakpoints.up('md')]: {
            display: 'flex',
        },
    },
    sectionMobile: {
        display: 'flex',
        [theme.breakpoints.up('md')]: {
            display: 'none',
        },
    },
    title: {
        display: 'none',
        [theme.breakpoints.up('sm')]: {
            display: 'block',
        },
    },
});

class PrimarySearchAppBar extends React.Component<AppBarProps, IAppBarState> {

    constructor(props: AppBarProps) {
        super(props);
        this.state = {
            mobileMoreAnchorEl: null,
        };
    }

    public handleMobileMenuOpen = (event: any) => {
        this.setState({mobileMoreAnchorEl: event.currentTarget});
    };

    public handleMobileMenuClose = () => {
        this.setState({mobileMoreAnchorEl: null});
    };

    public render() {
        const mobileMoreAnchorEl = this.state.mobileMoreAnchorEl;
        const classes = this.props.classes;
        const isMobileMenuOpen = Boolean(mobileMoreAnchorEl);
        const getClasses = () => {
            return classes as any
        };

        const renderMobileMenu = (
            <Menu
                anchorEl={mobileMoreAnchorEl}
                anchorOrigin={{vertical: 'top', horizontal: 'right'}}
                transformOrigin={{vertical: 'top', horizontal: 'right'}}
                open={isMobileMenuOpen}
                onClose={this.handleMobileMenuClose}
            >
                <MenuItem>
                    <IconButton color="inherit">
                        <Link to={"/code"} className={getClasses().linkWrapper}>
                            <CallbackIcon/>
                        </Link>
                    </IconButton>
                    <p>Code callbacks</p>
                </MenuItem>
                <MenuItem>
                    <IconButton color="inherit">
                        <Link to={"/history"} className={getClasses().linkWrapper}>
                            <Badge badgeContent={11} color="secondary">
                                <NotificationsIcon/>
                            </Badge>
                        </Link>
                    </IconButton>
                    <p>Notifications</p>
                </MenuItem>
            </Menu>
        );

        return (
            <div className={getClasses().root}>
                <AppBar position="static">
                    <Toolbar>
                        <IconButton className={getClasses().menuButton} color="inherit" aria-label="Home">
                            <Link to={"/"} className={getClasses().linkWrapper}>
                                <HomeIcon/>
                            </Link>
                        </IconButton>
                        <Typography className={getClasses().title} variant="h6" color="inherit" noWrap={true}>
                            FCMS
                        </Typography>
                        <div className={getClasses().search}>
                            <div className={getClasses().searchIcon}>
                                <SearchIcon/>
                            </div>
                            <InputBase
                                placeholder="Search…"
                                classes={{
                                    input: getClasses().inputInput,
                                    root: getClasses().inputRoot,
                                }}
                            />
                        </div>
                        <div className={getClasses().grow}/>
                        <div className={getClasses().sectionDesktop}>
                            <IconButton color={"inherit"}>
                                <Link to={"/code"} className={getClasses().linkWrapper}>
                                    <CallbackIcon/>
                                </Link>
                            </IconButton>
                            <IconButton color={"inherit"}>
                                <Link to={"/history"} className={getClasses().linkWrapper}>
                                    <Badge badgeContent={17} color="secondary">
                                        <NotificationsIcon/>
                                    </Badge>
                                </Link>
                            </IconButton>
                        </div>
                        <div className={getClasses().sectionMobile}>
                            <IconButton aria-haspopup="true" onClick={this.handleMobileMenuOpen} color="inherit">
                                <MoreIcon/>
                            </IconButton>
                        </div>
                    </Toolbar>
                </AppBar>
                {renderMobileMenu}
            </div>
        );
    }
}

interface IAppBarState {
    mobileMoreAnchorEl: any
}

export default withStyles(styles)(PrimarySearchAppBar);